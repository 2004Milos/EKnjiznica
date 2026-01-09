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
    public class ReviewsApiController : ControllerBase
    {
        private readonly LibraryContext _context;
        private readonly UserManager<IdentityUser> _userManager;

        public ReviewsApiController(LibraryContext context, UserManager<IdentityUser> userManager)
        {
            _context = context;
            _userManager = userManager;
        }

        /// <summary>
        /// Get reviews for a book (public endpoint)
        /// </summary>
        [HttpGet("book/{bookId}")]
        [AllowAnonymous]
        public async Task<ActionResult<ApiResponse<List<Review>>>> GetBookReviews(int bookId)
        {
            try
            {
                var reviews = await _context.Reviews
                    .Where(r => r.BookID == bookId)
                    .Include(r => r.User)
                    .OrderByDescending(r => r.ReviewDate)
                    .ToListAsync();

                return Ok(new ApiResponse<List<Review>>
                {
                    Success = true,
                    Data = reviews,
                    Message = "Reviews retrieved successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error retrieving reviews: {ex.Message}"
                });
            }
        }

        /// <summary>
        /// Get average rating for a book (public endpoint)
        /// </summary>
        [HttpGet("book/{bookId}/rating")]
        [AllowAnonymous]
        public async Task<ActionResult<ApiResponse<object>>> GetBookRating(int bookId)
        {
            try
            {
                var reviews = await _context.Reviews
                    .Where(r => r.BookID == bookId)
                    .ToListAsync();

                if (!reviews.Any())
                {
                    return Ok(new ApiResponse<object>
                    {
                        Success = true,
                        Data = new { averageRating = 0.0, reviewCount = 0 },
                        Message = "No reviews yet"
                    });
                }

                var averageRating = reviews.Average(r => r.Rating);
                var reviewCount = reviews.Count;

                return Ok(new ApiResponse<object>
                {
                    Success = true,
                    Data = new { averageRating = Math.Round(averageRating, 1), reviewCount },
                    Message = "Rating retrieved successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error retrieving rating: {ex.Message}"
                });
            }
        }

        /// <summary>
        /// Create a review (Member only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Member")]
        [HttpPost]
        public async Task<ActionResult<ApiResponse<Review>>> CreateReview([FromBody] CreateReviewRequest request)
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

                var userId = _userManager.GetUserId(User);
                if (string.IsNullOrEmpty(userId))
                {
                    return Unauthorized(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "User not authenticated"
                    });
                }

                // Check if user already reviewed this book
                var existingReview = await _context.Reviews
                    .FirstOrDefaultAsync(r => r.BookID == request.BookId && r.UserId == userId);

                if (existingReview != null)
                {
                    return BadRequest(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "You have already reviewed this book"
                    });
                }

                if (request.Rating < 1 || request.Rating > 5)
                {
                    return BadRequest(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "Rating must be between 1 and 5"
                    });
                }

                var review = new Review
                {
                    BookID = request.BookId,
                    UserId = userId,
                    Rating = request.Rating,
                    Comment = request.Comment,
                    ReviewDate = DateTime.Now
                };

                _context.Reviews.Add(review);
                await _context.SaveChangesAsync();

                // Reload with includes
                var createdReview = await _context.Reviews
                    .Include(r => r.User)
                    .Include(r => r.Book)
                    .FirstOrDefaultAsync(r => r.ID == review.ID);

                return Ok(new ApiResponse<Review>
                {
                    Success = true,
                    Data = createdReview!,
                    Message = "Review created successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error creating review: {ex.Message}"
                });
            }
        }

        /// <summary>
        /// Delete a review (Member only - own reviews)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Member")]
        [HttpDelete("{id}")]
        public async Task<ActionResult<ApiResponse<object>>> DeleteReview(int id)
        {
            try
            {
                var review = await _context.Reviews.FindAsync(id);
                if (review == null)
                {
                    return NotFound(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "Review not found"
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

                if (review.UserId != userId)
                {
                    return Forbid();
                }

                _context.Reviews.Remove(review);
                await _context.SaveChangesAsync();

                return Ok(new ApiResponse<object>
                {
                    Success = true,
                    Message = "Review deleted successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error deleting review: {ex.Message}"
                });
            }
        }

        /// <summary>
        /// Get my reviews (Member only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Member")]
        [HttpGet("my")]
        public async Task<ActionResult<ApiResponse<List<Review>>>> GetMyReviews()
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

                var reviews = await _context.Reviews
                    .Where(r => r.UserId == userId)
                    .Include(r => r.Book)
                    .OrderByDescending(r => r.ReviewDate)
                    .ToListAsync();

                return Ok(new ApiResponse<List<Review>>
                {
                    Success = true,
                    Data = reviews,
                    Message = "Reviews retrieved successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error retrieving reviews: {ex.Message}"
                });
            }
        }
    }

    public class CreateReviewRequest
    {
        public int BookId { get; set; }
        public int Rating { get; set; }
        public string? Comment { get; set; }
    }
}
