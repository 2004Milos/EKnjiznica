using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using EKnjiznica.Data;
using EKnjiznica.Models;

namespace EKnjiznica.Controllers
{
    [Authorize]
    public class LoansController : Controller
    {
        private readonly LibraryContext _context;
        private readonly UserManager<IdentityUser> _userManager;

        public LoansController(LibraryContext context, UserManager<IdentityUser> userManager)
        {
            _context = context;
            _userManager = userManager;
        }

        // GET: Loans (Librarian only - all loans)
        [Authorize(Roles = "Librarian")]
        public async Task<IActionResult> Index()
        {
            var loans = await _context.Loans
                .Include(l => l.Book)
                .Include(l => l.User)
                .OrderByDescending(l => l.LoanDate)
                .ToListAsync();

            return View(loans);
        }

        // GET: Loans/MyLoans (Member only - own loans)
        [Authorize(Roles = "Member")]
        public async Task<IActionResult> MyLoans()
        {
            var userId = _userManager.GetUserId(User);

            var loans = await _context.Loans
                .Where(l => l.UserId == userId)
                .Include(l => l.Book)
                .OrderByDescending(l => l.LoanDate)
                .ToListAsync();

            return View(loans);
        }

        // POST: Loans/Return/5 (Librarian only)
        [HttpPost]
        [ValidateAntiForgeryToken]
        [Authorize(Roles = "Librarian")]
        public async Task<IActionResult> Return(int id)
        {
            var loan = await _context.Loans
                .Include(l => l.Book)
                .FirstOrDefaultAsync(l => l.ID == id);

            if (loan == null)
            {
                return NotFound();
            }

            if (loan.Status == "Returned")
            {
                TempData["Error"] = "Loan already returned.";
                return RedirectToAction(nameof(Index));
            }

            loan.ReturnDate = DateTime.Now;
            loan.Status = "Returned";
            loan.Book.IsAvailable = true;

            await _context.SaveChangesAsync();
            TempData["Success"] = "Loan returned successfully.";

            return RedirectToAction(nameof(Index));
        }
    }
}
