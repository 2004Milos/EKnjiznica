using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using EKnjiznica.Data;
using EKnjiznica.Models;

namespace EKnjiznica.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Produces("application/json")]
    public class ReservationsApiController : ControllerBase
    {
        private readonly LibraryContext _context;
        private readonly UserManager<IdentityUser> _userManager;

        public ReservationsApiController(LibraryContext context, UserManager<IdentityUser> userManager)
        {
            _context = context;
            _userManager = userManager;
        }

        /// <summary>
        /// Make a reservation (Member only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Member")]
        [HttpPost("{bookId}")]
        public async Task<ActionResult<ApiResponse<Reservation>>> ReserveBook(int bookId)
        {
            try
            {
                var book = await _context.Books.FindAsync(bookId);
                if (book == null)
                {
                    return NotFound(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "Book not found"
                    });
                }

                if (!book.IsAvailable)
                {
                    return BadRequest(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "Book is not available"
                    });
                }

                var userId = _userManager.GetUserId(User);
                if (string.IsNullOrEmpty(userId))
                {
                    return Unauthorized(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "User not authenticated"
                    });
                }

                // Proveri da li korisnik već ima aktivnu rezervaciju za ovu knjigu
                var existingReservation = await _context.Reservations
                    .FirstOrDefaultAsync(r => r.BookID == bookId && r.UserId == userId && !r.IsApproved);
                
                if (existingReservation != null)
                {
                    return BadRequest(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "You already have an active reservation for this book"
                    });
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

                // Reload with includes
                var createdReservation = await _context.Reservations
                    .Include(r => r.Book)
                    .Include(r => r.User)
                    .FirstOrDefaultAsync(r => r.ID == reservation.ID);

                return Ok(new ApiResponse<Reservation>
                {
                    Success = true,
                    Data = createdReservation!,
                    Message = "Reservation created successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error creating reservation: {ex.Message}"
                });
            }
        }

        /// <summary>
        /// Get all reservations (Librarian only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Librarian")]
        [HttpGet]
        public async Task<ActionResult<ApiResponse<List<Reservation>>>> GetReservations()
        {
            try
            {
                var reservations = await _context.Reservations
                    .Include(r => r.Book)
                    .Include(r => r.User)
                    .ToListAsync();

                return Ok(new ApiResponse<List<Reservation>>
                {
                    Success = true,
                    Data = reservations,
                    Message = "Reservations retrieved successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error retrieving reservations: {ex.Message}"
                });
            }
        }

        /// <summary>
        /// Get my reservations (Member only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Member")]
        [HttpGet("my")]
        public async Task<ActionResult<ApiResponse<List<Reservation>>>> GetMyReservations()
        {
            try
            {
                var userId = _userManager.GetUserId(User);
                if (string.IsNullOrEmpty(userId))
                {
                    return Unauthorized(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "User not authenticated"
                    });
                }

                var reservations = await _context.Reservations
                    .Where(r => r.UserId == userId)
                    .Include(r => r.Book)
                    .ToListAsync();

                return Ok(new ApiResponse<List<Reservation>>
                {
                    Success = true,
                    Data = reservations,
                    Message = "Reservations retrieved successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error retrieving reservations: {ex.Message}"
                });
            }
        }

        /// <summary>
        /// Approve reservation and create loan (Librarian only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Librarian")]
        [HttpPost("approve/{reservationId}")]
        public async Task<ActionResult<ApiResponse<Loan>>> Approve(int reservationId)
        {
            try
            {
                var reservation = await _context.Reservations
                    .Include(r => r.Book)
                    .FirstOrDefaultAsync(r => r.ID == reservationId);

                if (reservation == null)
                {
                    return NotFound(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "Reservation not found"
                    });
                }

                if (reservation.IsApproved)
                {
                    return BadRequest(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "Reservation already approved"
                    });
                }

                if (!reservation.Book.IsAvailable)
                {
                    return BadRequest(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "Book is no longer available"
                    });
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

                // Reload loan with includes
                var createdLoan = await _context.Loans
                    .Include(l => l.Book)
                    .Include(l => l.User)
                    .FirstOrDefaultAsync(l => l.ID == loan.ID);

                return Ok(new ApiResponse<Loan>
                {
                    Success = true,
                    Data = createdLoan!,
                    Message = "Reservation approved and loan created successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error approving reservation: {ex.Message}"
                });
            }
        }
    }
}

