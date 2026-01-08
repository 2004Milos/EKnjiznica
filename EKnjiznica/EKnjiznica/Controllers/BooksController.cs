using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using EKnjiznica.Data;
using EKnjiznica.Models;
using System.Linq;

namespace EKnjiznica.Controllers
{
    [Authorize]
    public class BooksController : Controller
    {
        private readonly LibraryContext _context;

        public BooksController(LibraryContext context)
        {
            _context = context;
        }

        // GET: Books
        public async Task<IActionResult> Index()
        {
            return View(await _context.Books.ToListAsync());
        }

        // GET: Books/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var book = await _context.Books
                .FirstOrDefaultAsync(m => m.Id == id);
            if (book == null)
            {
                return NotFound();
            }

            var reviews = await _context.Reviews
                .Where(r => r.BookID == id)
                .Include(r => r.User)
                .OrderByDescending(r => r.ReviewDate)
                .ToListAsync();

            ViewBag.Reviews = reviews;

            // Calculate average rating
            if (reviews.Any())
            {
                ViewBag.AverageRating = reviews.Average(r => r.Rating);
                ViewBag.ReviewCount = reviews.Count;
            }
            else
            {
                ViewBag.AverageRating = 0;
                ViewBag.ReviewCount = 0;
            }

            return View(book);
        }

        // GET: Books/Create
        [Authorize(Roles = "Librarian")]
        public IActionResult Create()
        {
            return View();
        }

        // POST: Books/Create
        [HttpPost]
        [ValidateAntiForgeryToken]
        [Authorize(Roles = "Librarian")]
        public async Task<IActionResult> Create([Bind("Title,Author,Year,Genre,IsAvailable")] Book book)
        {
            if (ModelState.IsValid)
            {
                _context.Add(book);
                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Index));
            }
            return View(book);
        }

        // GET: Books/Edit/5
        [Authorize(Roles = "Librarian")]
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var book = await _context.Books.FindAsync(id);
            if (book == null)
            {
                return NotFound();
            }
            return View(book);
        }

        // POST: Books/Edit/5
        [HttpPost]
        [ValidateAntiForgeryToken]
        [Authorize(Roles = "Librarian")]
        public async Task<IActionResult> Edit(int id, [Bind("Id,Title,Author,Year,Genre,IsAvailable")] Book book)
        {
            if (id != book.Id)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                try
                {
                    _context.Update(book);
                    await _context.SaveChangesAsync();
                }
                catch (DbUpdateConcurrencyException)
                {
                    if (!BookExists(book.Id))
                    {
                        return NotFound();
                    }
                    else
                    {
                        throw;
                    }
                }
                return RedirectToAction(nameof(Index));
            }
            return View(book);
        }

        // POST: Books/Delete/5
        [HttpPost]
        [ValidateAntiForgeryToken]
        [Authorize(Roles = "Librarian")]
        public async Task<IActionResult> Delete(int id)
        {
            var book = await _context.Books.FindAsync(id);

            if (book == null)
            {
                TempData["Error"] = "Book not found.";
                return RedirectToAction(nameof(Index));
            }

            try
            {
                // Obriši povezane podatke
                var loans = await _context.Loans.Where(l => l.BookID == id).ToListAsync();
                if (loans.Any())
                {
                    _context.Loans.RemoveRange(loans);
                }

                var reservations = await _context.Reservations.Where(r => r.BookID == id).ToListAsync();
                if (reservations.Any())
                {
                    _context.Reservations.RemoveRange(reservations);
                }

                var reviews = await _context.Reviews.Where(r => r.BookID == id).ToListAsync();
                if (reviews.Any())
                {
                    _context.Reviews.RemoveRange(reviews);
                }

                // Obriši knjigu
                _context.Books.Remove(book);
                await _context.SaveChangesAsync();

                TempData["Success"] = "Book deleted successfully.";
            }
            catch (Exception ex)
            {
                TempData["Error"] = $"Error deleting book: {ex.Message}";
            }

            return RedirectToAction(nameof(Index));
        }

        private bool BookExists(int id)
        {
            return _context.Books.Any(e => e.Id == id);
        }
    }
}
