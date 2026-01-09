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
    public class StatisticsApiController : ControllerBase
    {
        private readonly LibraryContext _context;
        private readonly UserManager<IdentityUser> _userManager;

        public StatisticsApiController(LibraryContext context, UserManager<IdentityUser> userManager)
        {
            _context = context;
            _userManager = userManager;
        }

        /// <summary>
        /// Get statistics for librarian (Librarian only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Librarian")]
        [HttpGet("librarian")]
        public async Task<ActionResult<ApiResponse<object>>> GetLibrarianStatistics()
        {
            try
            {
                var totalBooks = await _context.Books.CountAsync();
                var availableBooks = await _context.Books.CountAsync(b => b.IsAvailable);
                var totalLoans = await _context.Loans.CountAsync();
                var activeLoans = await _context.Loans.CountAsync(l => l.Status == "Active");
                var overdueLoans = await _context.Loans.CountAsync(l => l.Status == "Overdue");
                var pendingReservations = await _context.Reservations.CountAsync(r => !r.IsApproved);
                var members = await _userManager.GetUsersInRoleAsync("Member");
                var totalMembers = members.Count;

                return Ok(new ApiResponse<object>
                {
                    Success = true,
                    Data = new
                    {
                        totalBooks,
                        availableBooks,
                        totalLoans,
                        activeLoans,
                        overdueLoans,
                        pendingReservations,
                        totalMembers
                    },
                    Message = "Statistics retrieved successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error retrieving statistics: {ex.Message}"
                });
            }
        }

        /// <summary>
        /// Get statistics for member (Member only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Member")]
        [HttpGet("member")]
        public async Task<ActionResult<ApiResponse<object>>> GetMemberStatistics()
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

                var myActiveLoans = await _context.Loans
                    .CountAsync(l => l.UserId == userId && l.Status == "Active");
                var myPendingReservations = await _context.Reservations
                    .CountAsync(r => r.UserId == userId && !r.IsApproved);
                var availableBooks = await _context.Books.CountAsync(b => b.IsAvailable);

                return Ok(new ApiResponse<object>
                {
                    Success = true,
                    Data = new
                    {
                        myActiveLoans,
                        myPendingReservations,
                        availableBooks
                    },
                    Message = "Statistics retrieved successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error retrieving statistics: {ex.Message}"
                });
            }
        }
    }
}
