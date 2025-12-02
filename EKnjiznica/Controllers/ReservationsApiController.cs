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
    public class ReservationsApiController : ControllerBase
    {
        private readonly LibraryContext _context;
        private readonly UserManager<IdentityUser> _userManager;

        public ReservationsApiController(LibraryContext context, UserManager<IdentityUser> userManager)
        {
            _context = context;
            _userManager = userManager;
        }

        // Member makes a reservation
        [Authorize(Roles = "Member")]
        [HttpPost("{bookId}")]
        public async Task<IActionResult> ReserveBook(int bookId)
        {
            var book = await _context.Books.FindAsync(bookId);
            if (book == null || !book.IsAvailable)
                return BadRequest("Book unavailable.");

            var userId = _userManager.GetUserId(User);

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

            return Ok(reservation);
        }

        // Librarian sees all reservations
        [Authorize(Roles = "Librarian")]
        [HttpGet]
        public async Task<IActionResult> GetReservations()
        {
            var res = await _context.Reservations
                .Include(r => r.Book)
                .Include(r => r.User)
                .ToListAsync();

            return Ok(res);
        }

        // Librarian approves reservation -> creates a loan
        [Authorize(Roles = "Librarian")]
        [HttpPost("approve/{reservationId}")]
        public async Task<IActionResult> Approve(int reservationId)
        {
            var res = await _context.Reservations
                .Include(r => r.Book)
                .FirstOrDefaultAsync(r => r.ID == reservationId);

            if (res == null) return NotFound();
            if (res.IsApproved) return BadRequest("Already approved.");

            res.IsApproved = true;
            res.Book.IsAvailable = false;

            var loan = new Loan
            {
                BookID = res.BookID,
                UserId = res.UserId,
                LoanDate = DateTime.Now,
                DueDate = DateTime.Now.AddDays(14),
                Status = "Active"
            };

            _context.Loans.Add(loan);
            await _context.SaveChangesAsync();

            return Ok(loan);
        }
    }
}

