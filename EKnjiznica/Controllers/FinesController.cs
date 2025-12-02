using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using EKnjiznica.Data;
using EKnjiznica.Models;

namespace EKnjiznica.Controllers
{
    [Authorize]
    public class FinesController : Controller
    {
        private readonly LibraryContext _context;
        private readonly UserManager<IdentityUser> _userManager;

        public FinesController(LibraryContext context, UserManager<IdentityUser> userManager)
        {
            _context = context;
            _userManager = userManager;
        }

        // GET: Fines (Librarian - all fines)
        [Authorize(Roles = "Librarian")]
        public async Task<IActionResult> Index()
        {
            var fines = await _context.Fines
                .Include(f => f.User)
                .OrderByDescending(f => f.IssueDate)
                .ToListAsync();

            return View(fines);
        }

        // GET: Fines/MyFines (Member - own fines)
        [Authorize(Roles = "Member")]
        public async Task<IActionResult> MyFines()
        {
            var userId = _userManager.GetUserId(User);

            var fines = await _context.Fines
                .Where(f => f.UserId == userId)
                .OrderByDescending(f => f.IssueDate)
                .ToListAsync();

            return View(fines);
        }

        // GET: Fines/Create
        [Authorize(Roles = "Librarian")]
        public async Task<IActionResult> Create()
        {
            var members = await _userManager.GetUsersInRoleAsync("Member");
            ViewBag.Members = members;
            return View();
        }

        // POST: Fines/Create
        [HttpPost]
        [ValidateAntiForgeryToken]
        [Authorize(Roles = "Librarian")]
        public async Task<IActionResult> Create([Bind("UserId,Amount,Reason")] Fine fine)
        {
            if (string.IsNullOrEmpty(fine.UserId))
            {
                ModelState.AddModelError("UserId", "Please select a member.");
            }

            if (fine.Amount <= 0)
            {
                ModelState.AddModelError("Amount", "Amount must be greater than 0.");
            }

            if (string.IsNullOrWhiteSpace(fine.Reason))
            {
                ModelState.AddModelError("Reason", "Reason is required.");
            }

            if (ModelState.IsValid)
            {
                fine.IssueDate = DateTime.Now;
                fine.IsPaid = false;
                _context.Add(fine);
                await _context.SaveChangesAsync();
                TempData["Success"] = "Fine created successfully.";
                return RedirectToAction(nameof(Index));
            }
            
            var members = await _userManager.GetUsersInRoleAsync("Member");
            ViewBag.Members = members;
            return View(fine);
        }

        // POST: Fines/MarkAsPaid/5
        [HttpPost]
        [ValidateAntiForgeryToken]
        [Authorize(Roles = "Librarian")]
        public async Task<IActionResult> MarkAsPaid(int id)
        {
            var fine = await _context.Fines.FindAsync(id);
            if (fine == null)
            {
                return NotFound();
            }

            if (fine.IsPaid)
            {
                TempData["Error"] = "Fine is already paid.";
                return RedirectToAction(nameof(Index));
            }

            fine.IsPaid = true;
            fine.PaidDate = DateTime.Now;
            await _context.SaveChangesAsync();

            TempData["Success"] = "Fine marked as paid.";
            return RedirectToAction(nameof(Index));
        }
    }
}

