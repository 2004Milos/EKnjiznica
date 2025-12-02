using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using EKnjiznica.Data;
using EKnjiznica.Models;

namespace EKnjiznica.Controllers
{
    [Authorize(Roles = "Member")]
    public class ReviewsController : Controller
    {
        private readonly LibraryContext _context;
        private readonly UserManager<IdentityUser> _userManager;

        public ReviewsController(LibraryContext context, UserManager<IdentityUser> userManager)
        {
            _context = context;
            _userManager = userManager;
        }

        // POST: Reviews/Create
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create(int bookId, int rating, string? comment)
        {
            var book = await _context.Books.FindAsync(bookId);
            if (book == null)
            {
                TempData["Error"] = "Book not found.";
                return RedirectToAction("Details", "Books", new { id = bookId });
            }

            var user = await _userManager.GetUserAsync(User);
            if (user == null)
            {
                TempData["Error"] = "User not found.";
                return RedirectToAction("Details", "Books", new { id = bookId });
            }

            var userId = user.Id;

            // Check if user already reviewed this book
            var existingReview = await _context.Reviews
                .FirstOrDefaultAsync(r => r.BookID == bookId && r.UserId == userId);

            if (existingReview != null)
            {
                TempData["Error"] = "You have already reviewed this book.";
                return RedirectToAction("Details", "Books", new { id = bookId });
            }

            var review = new Review
            {
                BookID = bookId,
                UserId = userId,
                Rating = rating,
                Comment = comment,
                ReviewDate = DateTime.Now
            };

            _context.Reviews.Add(review);
            await _context.SaveChangesAsync();

            TempData["Success"] = "Review added successfully.";
            return RedirectToAction("Details", "Books", new { id = bookId });
        }

        // POST: Reviews/Delete/5
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Delete(int id)
        {
            var review = await _context.Reviews.FindAsync(id);
            if (review == null)
            {
                return NotFound();
            }

            var user = await _userManager.GetUserAsync(User);
            if (user == null)
            {
                TempData["Error"] = "User not found.";
                return RedirectToAction("Details", "Books", new { id = review.BookID });
            }

            var userId = user.Id;
            if (review.UserId != userId)
            {
                TempData["Error"] = "You can only delete your own reviews.";
                return RedirectToAction("Details", "Books", new { id = review.BookID });
            }

            var bookId = review.BookID;
            _context.Reviews.Remove(review);
            await _context.SaveChangesAsync();

            TempData["Success"] = "Review deleted successfully.";
            return RedirectToAction("Details", "Books", new { id = bookId });
        }
    }
}

