using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using EKnjiznica.Data;
using EKnjiznica.Models;

namespace EKnjiznica.Controllers
{
    [Authorize]
    public class ReservationsController : Controller
    {
        private readonly LibraryContext _context;
        private readonly UserManager<IdentityUser> _userManager;

        public ReservationsController(LibraryContext context, UserManager<IdentityUser> userManager)
        {
            _context = context;
            _userManager = userManager;
        }

        // GET: Reservations (Librarian only - all reservations)
        [Authorize(Roles = "Librarian")]
        public async Task<IActionResult> Index()
        {
            var reservations = await _context.Reservations
                .Include(r => r.Book)
                .Include(r => r.User)
                .Where(r => !r.IsApproved)
                .OrderBy(r => r.ReservationDate)
                .ToListAsync();

            return View(reservations);
        }

        // GET: Reservations/MyReservations (Member only - own reservations)
        [Authorize(Roles = "Member")]
        public async Task<IActionResult> MyReservations()
        {
            var userId = _userManager.GetUserId(User);

            var reservations = await _context.Reservations
                .Where(r => r.UserId == userId)
                .Include(r => r.Book)
                .OrderByDescending(r => r.ReservationDate)
                .ToListAsync();

            return View(reservations);
        }

        // POST: Reservations/Reserve (Member only)
        [HttpPost]
        [ValidateAntiForgeryToken]
        [Authorize(Roles = "Member")]
        public async Task<IActionResult> Reserve(int bookId)
        {
            var book = await _context.Books.FindAsync(bookId);
            if (book == null)
            {
                TempData["Error"] = "Book not found.";
                return RedirectToAction("Index", "Books");
            }

            if (!book.IsAvailable)
            {
                TempData["Error"] = "Book is not available.";
                return RedirectToAction("Index", "Books");
            }

            var userId = _userManager.GetUserId(User);

            // Check if user already has a reservation for this book
            var existingReservation = await _context.Reservations
                .FirstOrDefaultAsync(r => r.UserId == userId && r.BookID == bookId && !r.IsApproved);

            if (existingReservation != null)
            {
                TempData["Error"] = "You already have a reservation for this book.";
                return RedirectToAction("Index", "Books");
            }

            var reservation = new Reservation
            {
                BookID = bookId,
                UserId = userId,
                ReservationDate = DateTime.Now,
                ExpiryDate = DateTime.Now.AddDays(7),
                IsApproved = false
            };

            _context.Reservations.Add(reservation);
            await _context.SaveChangesAsync();

            TempData["Success"] = "Reservation created successfully.";
            return RedirectToAction("Index", "Books");
        }

        // POST: Reservations/Approve/5 (Librarian only)
        [HttpPost]
        [ValidateAntiForgeryToken]
        [Authorize(Roles = "Librarian")]
        public async Task<IActionResult> Approve(int id)
        {
            var reservation = await _context.Reservations
                .Include(r => r.Book)
                .FirstOrDefaultAsync(r => r.ID == id);

            if (reservation == null)
            {
                return NotFound();
            }

            if (reservation.IsApproved)
            {
                TempData["Error"] = "Reservation already approved.";
                return RedirectToAction(nameof(Index));
            }

            if (!reservation.Book.IsAvailable)
            {
                TempData["Error"] = "Book is not available.";
                return RedirectToAction(nameof(Index));
            }

            reservation.IsApproved = true;
            reservation.Book.IsAvailable = false;

            var loan = new Loan
            {
                BookID = reservation.BookID,
                UserId = reservation.UserId,
                LoanDate = DateTime.Now,
                DueDate = DateTime.Now.AddDays(14),
                Status = "Active"
            };

            _context.Loans.Add(loan);
            await _context.SaveChangesAsync();

            TempData["Success"] = "Reservation approved and loan created.";
            return RedirectToAction(nameof(Index));
        }
    }
}
