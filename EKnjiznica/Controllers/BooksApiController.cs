using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using EKnjiznica.Data;
using EKnjiznica.Models;

namespace EKnjiznica.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class BooksApiController : ControllerBase
    {
        private readonly LibraryContext _context;

        public BooksApiController(LibraryContext context)
        {
            _context = context;
        }

        // Anyone can list books
        [HttpGet]
        public async Task<IActionResult> GetBooks()
        {
            return Ok(await _context.Books.ToListAsync());
        }

        // Librarian only
        [Authorize(Roles = "Librarian")]
        [HttpPost]
        public async Task<IActionResult> AddBook(Book book)
        {
            _context.Books.Add(book);
            await _context.SaveChangesAsync();
            return Ok(book);
        }

        // Librarian only
        [Authorize(Roles = "Librarian")]
        [HttpPut("{id}")]
        public async Task<IActionResult> UpdateBook(int id, Book updated)
        {
            var book = await _context.Books.FindAsync(id);
            if (book == null) return NotFound();

            book.Title = updated.Title;
            book.Author = updated.Author;
            book.Year = updated.Year;
            book.Genre = updated.Genre;

            await _context.SaveChangesAsync();
            return Ok(book);
        }

        // Librarian only
        [Authorize(Roles = "Librarian")]
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteBook(int id)
        {
            var book = await _context.Books.FindAsync(id);
            if (book == null) return NotFound();

            _context.Books.Remove(book);
            await _context.SaveChangesAsync();
            return Ok();
        }
    }
}

