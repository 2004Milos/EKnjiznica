using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using EKnjiznica.Data;
using EKnjiznica.Models;

namespace EKnjiznica.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class LoansApiController : ControllerBase
    {
        private readonly LibraryContext _context;
        private readonly UserManager<IdentityUser> _userManager;

        public LoansApiController(LibraryContext context, UserManager<IdentityUser> userManager)
        {
            _context = context;
            _userManager = userManager;
        }

        // ============================================================
        // LIBRARIAN: get all loans
        // ============================================================
        [Authorize(Roles = "Librarian")]
        [HttpGet]
        public async Task<IActionResult> GetAllLoans()
        {
            var loans = await _context.Loans
                .Include(l => l.Book)
                .Include(l => l.User)
                .ToListAsync();

            return Ok(loans);
        }

        // ============================================================
        // MEMBER: get my own loans
        // ============================================================
        [Authorize(Roles = "Member")]
        [HttpGet("my")]
        public async Task<IActionResult> GetMyLoans()
        {
            var userId = _userManager.GetUserId(User);

            var loans = await _context.Loans
                .Where(l => l.UserId == userId)
                .Include(l => l.Book)
                .ToListAsync();

            return Ok(loans);
        }

        // ============================================================
        // LIBRARIAN: manually create a loan (optional)
        // ============================================================
        [Authorize(Roles = "Librarian")]
        [HttpPost("create")]
        public async Task<IActionResult> CreateLoan(int bookId, string userId, int days = 14)
        {
            var book = await _context.Books.FindAsync(bookId);
            if (book == null) return NotFound("Book not found.");
            if (!book.IsAvailable) return BadRequest("Book is not available.");

            var loan = new Loan
            {
                BookID = bookId,
                UserId = userId,
                LoanDate = DateTime.Now,
                DueDate = DateTime.Now.AddDays(days),
                Status = "Active"
            };

            book.IsAvailable = false;

            _context.Loans.Add(loan);
            await _context.SaveChangesAsync();

            return Ok(loan);
        }

        // ============================================================
        // LIBRARIAN: return a loan
        // ============================================================
        [Authorize(Roles = "Librarian")]
        [HttpPost("return/{loanId}")]
        public async Task<IActionResult> ReturnLoan(int loanId)
        {
            var loan = await _context.Loans
                .Include(l => l.Book)
                .FirstOrDefaultAsync(l => l.ID == loanId);

            if (loan == null) return NotFound("Loan not found.");
            if (loan.Status == "Returned") return BadRequest("Already returned.");

            loan.ReturnDate = DateTime.Now;
            loan.Status = "Returned";
            loan.Book.IsAvailable = true;

            await _context.SaveChangesAsync();
            return Ok(loan);
        }

        // ============================================================
        // LIBRARIAN: mark overdue loans
        // ============================================================
        [Authorize(Roles = "Librarian")]
        [HttpPost("check-overdue")]
        public async Task<IActionResult> CheckOverdue()
        {
            var overdueLoans = await _context.Loans
                .Where(l => l.Status == "Active" && l.DueDate < DateTime.Now)
                .ToListAsync();

            foreach (var loan in overdueLoans)
            {
                loan.Status = "Overdue";
            }

            await _context.SaveChangesAsync();
            return Ok(overdueLoans);
        }
    }
}

