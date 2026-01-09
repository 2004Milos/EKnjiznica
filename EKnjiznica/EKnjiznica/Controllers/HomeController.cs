using EKnjiznica.Models;
using EKnjiznica.Data;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using System.Diagnostics;

namespace EKnjiznica.Controllers
{
    public class HomeController : Controller
    {
        private readonly ILogger<HomeController> _logger;
        private readonly LibraryContext _context;
        private readonly UserManager<IdentityUser> _userManager;

        public HomeController(ILogger<HomeController> logger, LibraryContext context, UserManager<IdentityUser> userManager)
        {
            _logger = logger;
            _context = context;
            _userManager = userManager;
        }

        public async Task<IActionResult> Index()
        {
            if (User.Identity?.IsAuthenticated == true)
            {
                if (User.IsInRole("Librarian"))
                {
                    ViewBag.TotalBooks = await _context.Books.CountAsync();
                    ViewBag.AvailableBooks = await _context.Books.CountAsync(b => b.IsAvailable);
                    ViewBag.TotalLoans = await _context.Loans.CountAsync();
                    ViewBag.ActiveLoans = await _context.Loans.CountAsync(l => l.Status == "Active");
                    ViewBag.PendingReservations = await _context.Reservations.CountAsync(r => !r.IsApproved);
                    var members = await _userManager.GetUsersInRoleAsync("Member");
                    var librarians = await _userManager.GetUsersInRoleAsync("Librarian");
                    ViewBag.TotalMembers = members.Count + librarians.Count;
                }
                else if (User.IsInRole("Member"))
                {
                    var userId = _userManager.GetUserId(User);
                    ViewBag.MyActiveLoans = await _context.Loans
                        .CountAsync(l => l.UserId == userId && l.Status == "Active");
                    ViewBag.MyPendingReservations = await _context.Reservations
                        .CountAsync(r => r.UserId == userId && !r.IsApproved);
                    ViewBag.AvailableBooks = await _context.Books.CountAsync(b => b.IsAvailable);
                }
            }
            return View();
        }

        public IActionResult Privacy()
        {
            return View();
        }

        [ResponseCache(Duration = 0, Location = ResponseCacheLocation.None, NoStore = true)]
        public IActionResult Error()
        {
            return View(new ErrorViewModel { RequestId = Activity.Current?.Id ?? HttpContext.TraceIdentifier });
        }
    }
}
