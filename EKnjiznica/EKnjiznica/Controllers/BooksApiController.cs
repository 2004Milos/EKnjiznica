using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using EKnjiznica.Data;
using EKnjiznica.Models;
using System.ComponentModel.DataAnnotations;

namespace EKnjiznica.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Produces("application/json")]
    public class BooksApiController : ControllerBase
    {
        private readonly LibraryContext _context;

        public BooksApiController(LibraryContext context)
        {
            _context = context;
        }

        /// <summary>
        /// Get all books (public endpoint)
        /// </summary>
        [HttpGet]
        [AllowAnonymous]
        public async Task<ActionResult<ApiResponse<List<Book>>>> GetBooks([FromQuery] string? search = null)
        {
            try
            {
                var query = _context.Books.AsQueryable();
                
                if (!string.IsNullOrEmpty(search))
                {
                    search = search.ToLower();
                    query = query.Where(b => 
                        b.Title.ToLower().Contains(search) || 
                        b.Author.ToLower().Contains(search) || 
                        b.Genre.ToLower().Contains(search));
                }
                
                var books = await query.ToListAsync();
                return Ok(new ApiResponse<List<Book>>
                {
                    Success = true,
                    Data = books,
                    Message = "Books retrieved successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error retrieving books: {ex.Message}"
                });
            }
        }

        /// <summary>
        /// Get book by ID (public endpoint)
        /// </summary>
        [HttpGet("{id}")]
        [AllowAnonymous]
        public async Task<ActionResult<ApiResponse<Book>>> GetBook(int id)
        {
            try
            {
                var book = await _context.Books.FindAsync(id);
                if (book == null)
                {
                    return NotFound(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "Book not found"
                    });
                }

                return Ok(new ApiResponse<Book>
                {
                    Success = true,
                    Data = book,
                    Message = "Book retrieved successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error retrieving book: {ex.Message}"
                });
            }
        }

        /// <summary>
        /// Add a new book (Librarian only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Librarian")]
        [HttpPost]
        public async Task<ActionResult<ApiResponse<Book>>> AddBook([FromBody] Book book)
        {
            try
            {
                if (!ModelState.IsValid)
                {
                    return BadRequest(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "Invalid book data",
                        Errors = ModelState.Values.SelectMany(v => v.Errors.Select(e => e.ErrorMessage))
                    });
                }

                _context.Books.Add(book);
                await _context.SaveChangesAsync();

                return CreatedAtAction(nameof(GetBook), new { id = book.Id }, new ApiResponse<Book>
                {
                    Success = true,
                    Data = book,
                    Message = "Book created successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error creating book: {ex.Message}"
                });
            }
        }

        /// <summary>
        /// Update a book (Librarian only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Librarian")]
        [HttpPut("{id}")]
        public async Task<ActionResult<ApiResponse<Book>>> UpdateBook(int id, [FromBody] Book updated)
        {
            try
            {
                if (!ModelState.IsValid)
                {
                    return BadRequest(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "Invalid book data",
                        Errors = ModelState.Values.SelectMany(v => v.Errors.Select(e => e.ErrorMessage))
                    });
                }

                var book = await _context.Books.FindAsync(id);
                if (book == null)
                {
                    return NotFound(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "Book not found"
                    });
                }

                book.Title = updated.Title;
                book.Author = updated.Author;
                book.Year = updated.Year;
                book.Genre = updated.Genre;
                book.IsAvailable = updated.IsAvailable;

                await _context.SaveChangesAsync();

                return Ok(new ApiResponse<Book>
                {
                    Success = true,
                    Data = book,
                    Message = "Book updated successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error updating book: {ex.Message}"
                });
            }
        }

        /// <summary>
        /// Delete a book (Librarian only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Librarian")]
        [HttpDelete("{id}")]
        public async Task<ActionResult<ApiResponse<object>>> DeleteBook(int id)
        {
            try
            {
                var book = await _context.Books.FindAsync(id);
                if (book == null)
                {
                    return NotFound(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "Book not found"
                    });
                }

                // Obriši povezane podatke
                var loans = await _context.Loans.Where(l => l.BookID == id).ToListAsync();
                if (loans.Any())
                {
                    _context.Loans.RemoveRange(loans);
                }

                var reservations = await _context.Reservations.Where(r => r.BookID == id).ToListAsync();
                if (reservations.Any())
                {
                    _context.Reservations.RemoveRange(reservations);
                }

                var reviews = await _context.Reviews.Where(r => r.BookID == id).ToListAsync();
                if (reviews.Any())
                {
                    _context.Reviews.RemoveRange(reviews);
                }

                _context.Books.Remove(book);
                await _context.SaveChangesAsync();

                return Ok(new ApiResponse<object>
                {
                    Success = true,
                    Message = "Book deleted successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error deleting book: {ex.Message}"
                });
            }
        }
    }

}

