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
    public class LoansApiController : ControllerBase
    {
        private readonly LibraryContext _context;
        private readonly UserManager<IdentityUser> _userManager;

        public LoansApiController(LibraryContext context, UserManager<IdentityUser> userManager)
        {
            _context = context;
            _userManager = userManager;
        }

        /// <summary>
        /// Get all loans (Librarian only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Librarian")]
        [HttpGet]
        public async Task<ActionResult<ApiResponse<List<Loan>>>> GetAllLoans()
        {
            try
            {
                var loans = await _context.Loans
                    .Include(l => l.Book)
                    .Include(l => l.User)
                    .ToListAsync();

                return Ok(new ApiResponse<List<Loan>>
                {
                    Success = true,
                    Data = loans,
                    Message = "Loans retrieved successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error retrieving loans: {ex.Message}"
                });
            }
        }

        /// <summary>
        /// Get my own loans (Member only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Member")]
        [HttpGet("my")]
        public async Task<ActionResult<ApiResponse<List<Loan>>>> GetMyLoans()
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

                var loans = await _context.Loans
                    .Where(l => l.UserId == userId)
                    .Include(l => l.Book)
                    .ToListAsync();

                return Ok(new ApiResponse<List<Loan>>
                {
                    Success = true,
                    Data = loans,
                    Message = "Loans retrieved successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error retrieving loans: {ex.Message}"
                });
            }
        }

        /// <summary>
        /// Create a new loan (Librarian only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Librarian")]
        [HttpPost("create")]
        public async Task<ActionResult<ApiResponse<Loan>>> CreateLoan([FromBody] CreateLoanRequest request)
        {
            try
            {
                var book = await _context.Books.FindAsync(request.BookId);
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

                var user = await _userManager.FindByIdAsync(request.UserId);
                if (user == null)
                {
                    return NotFound(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "User not found"
                    });
                }

                var loan = new Loan
                {
                    BookID = request.BookId,
                    UserId = request.UserId,
                    LoanDate = DateTime.Now,
                    DueDate = DateTime.Now.AddDays(request.Days),
                    Status = "Active"
                };

                book.IsAvailable = false;

                _context.Loans.Add(loan);
                await _context.SaveChangesAsync();

                // Reload with includes
                var createdLoan = await _context.Loans
                    .Include(l => l.Book)
                    .Include(l => l.User)
                    .FirstOrDefaultAsync(l => l.ID == loan.ID);

                return Ok(new ApiResponse<Loan>
                {
                    Success = true,
                    Data = createdLoan!,
                    Message = "Loan created successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error creating loan: {ex.Message}"
                });
            }
        }

        /// <summary>
        /// Return a loan (Librarian only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Librarian")]
        [HttpPost("return/{loanId}")]
        public async Task<ActionResult<ApiResponse<Loan>>> ReturnLoan(int loanId)
        {
            try
            {
                var loan = await _context.Loans
                    .Include(l => l.Book)
                    .FirstOrDefaultAsync(l => l.ID == loanId);

                if (loan == null)
                {
                    return NotFound(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "Loan not found"
                    });
                }

                if (loan.Status == "Returned")
                {
                    return BadRequest(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "Loan already returned"
                    });
                }

                loan.ReturnDate = DateTime.Now;
                loan.Status = "Returned";
                loan.Book.IsAvailable = true;

                await _context.SaveChangesAsync();

                return Ok(new ApiResponse<Loan>
                {
                    Success = true,
                    Data = loan,
                    Message = "Loan returned successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error returning loan: {ex.Message}"
                });
            }
        }

        /// <summary>
        /// Check and mark overdue loans (Librarian only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Librarian")]
        [HttpPost("check-overdue")]
        public async Task<ActionResult<ApiResponse<List<Loan>>>> CheckOverdue()
        {
            try
            {
                var overdueLoans = await _context.Loans
                    .Where(l => l.Status == "Active" && l.DueDate < DateTime.Now)
                    .Include(l => l.Book)
                    .Include(l => l.User)
                    .ToListAsync();

                foreach (var loan in overdueLoans)
                {
                    loan.Status = "Overdue";
                }

                await _context.SaveChangesAsync();

                return Ok(new ApiResponse<List<Loan>>
                {
                    Success = true,
                    Data = overdueLoans,
                    Message = $"Marked {overdueLoans.Count} loans as overdue"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error checking overdue loans: {ex.Message}"
                });
            }
        }
    }

    public class CreateLoanRequest
    {
        public int BookId { get; set; }
        public string UserId { get; set; } = string.Empty;
        public int Days { get; set; } = 14;
    }
}


