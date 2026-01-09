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
    public class FinesApiController : ControllerBase
    {
        private readonly LibraryContext _context;
        private readonly UserManager<IdentityUser> _userManager;

        public FinesApiController(LibraryContext context, UserManager<IdentityUser> userManager)
        {
            _context = context;
            _userManager = userManager;
        }

        /// <summary>
        /// Get all fines (Librarian only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Librarian")]
        [HttpGet]
        public async Task<ActionResult<ApiResponse<List<Fine>>>> GetAllFines()
        {
            try
            {
                var fines = await _context.Fines
                    .Include(f => f.User)
                    .OrderByDescending(f => f.IssueDate)
                    .ToListAsync();

                return Ok(new ApiResponse<List<Fine>>
                {
                    Success = true,
                    Data = fines,
                    Message = "Fines retrieved successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error retrieving fines: {ex.Message}"
                });
            }
        }

        /// <summary>
        /// Get my fines (Member only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Member")]
        [HttpGet("my")]
        public async Task<ActionResult<ApiResponse<List<Fine>>>> GetMyFines()
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

                var fines = await _context.Fines
                    .Where(f => f.UserId == userId)
                    .OrderByDescending(f => f.IssueDate)
                    .ToListAsync();

                return Ok(new ApiResponse<List<Fine>>
                {
                    Success = true,
                    Data = fines,
                    Message = "Fines retrieved successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error retrieving fines: {ex.Message}"
                });
            }
        }

        /// <summary>
        /// Create a fine (Librarian only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Librarian")]
        [HttpPost]
        public async Task<ActionResult<ApiResponse<Fine>>> CreateFine([FromBody] CreateFineRequest request)
        {
            try
            {
                if (request.Amount <= 0)
                {
                    return BadRequest(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "Amount must be greater than 0"
                    });
                }

                if (string.IsNullOrWhiteSpace(request.Reason))
                {
                    return BadRequest(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "Reason is required"
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

                var fine = new Fine
                {
                    UserId = request.UserId,
                    Amount = request.Amount,
                    Reason = request.Reason,
                    IssueDate = DateTime.Now,
                    IsPaid = false
                };

                _context.Fines.Add(fine);
                await _context.SaveChangesAsync();

                // Reload with includes
                var createdFine = await _context.Fines
                    .Include(f => f.User)
                    .FirstOrDefaultAsync(f => f.ID == fine.ID);

                return Ok(new ApiResponse<Fine>
                {
                    Success = true,
                    Data = createdFine!,
                    Message = "Fine created successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error creating fine: {ex.Message}"
                });
            }
        }

        /// <summary>
        /// Mark fine as paid (Librarian only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Librarian")]
        [HttpPost("{id}/mark-paid")]
        public async Task<ActionResult<ApiResponse<Fine>>> MarkAsPaid(int id)
        {
            try
            {
                var fine = await _context.Fines.FindAsync(id);
                if (fine == null)
                {
                    return NotFound(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "Fine not found"
                    });
                }

                if (fine.IsPaid)
                {
                    return BadRequest(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "Fine is already paid"
                    });
                }

                fine.IsPaid = true;
                fine.PaidDate = DateTime.Now;

                await _context.SaveChangesAsync();

                // Reload with includes
                var updatedFine = await _context.Fines
                    .Include(f => f.User)
                    .FirstOrDefaultAsync(f => f.ID == fine.ID);

                return Ok(new ApiResponse<Fine>
                {
                    Success = true,
                    Data = updatedFine!,
                    Message = "Fine marked as paid successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error updating fine: {ex.Message}"
                });
            }
        }
    }

    public class CreateFineRequest
    {
        public string UserId { get; set; } = string.Empty;
        public decimal Amount { get; set; }
        public string Reason { get; set; } = string.Empty;
    }
}
