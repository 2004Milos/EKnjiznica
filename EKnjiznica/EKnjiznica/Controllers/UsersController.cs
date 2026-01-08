using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace EKnjiznica.Controllers
{
    [Authorize(Roles = "Librarian")]
    public class UsersController : Controller
    {
        private readonly UserManager<IdentityUser> _userManager;
        private readonly RoleManager<IdentityRole> _roleManager;

        public UsersController(UserManager<IdentityUser> userManager, RoleManager<IdentityRole> roleManager)
        {
            _userManager = userManager;
            _roleManager = roleManager;
        }

        // GET: Users
        public async Task<IActionResult> Index()
        {
            var members = await _userManager.GetUsersInRoleAsync("Member");
            var librarians = await _userManager.GetUsersInRoleAsync("Librarian");

            return View(members.Concat(librarians));
        }

        // GET: Users/Details/5
        public async Task<IActionResult> Details(string id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var user = await _userManager.FindByIdAsync(id);
            if (user == null)
            {
                return NotFound();
            }

            var roles = await _userManager.GetRolesAsync(user);
            ViewBag.Roles = roles;
            return View(user);
        }

        // GET: Users/Create
        public IActionResult Create()
        {
            return View();
        }

        // POST: Users/Create
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create(string email, string password, string role)
        {
            if (string.IsNullOrEmpty(email) || string.IsNullOrEmpty(password))
            {
                TempData["Error"] = "Email and password are required.";
                return View();
            }

            // Proveri da li korisnik već postoji
            var existingUser = await _userManager.FindByEmailAsync(email);
            if (existingUser != null)
            {
                TempData["Error"] = "User with this email already exists.";
                return View();
            }

            var user = new IdentityUser
            {
                UserName = email,
                Email = email,
                EmailConfirmed = true
            };

            var result = await _userManager.CreateAsync(user, password);
            if (result.Succeeded)
            {
                // Osiguraj da se role uvek dodeli - default je Member ako nije izabrano
                string roleToAssign = !string.IsNullOrEmpty(role) && (role == "Librarian" || role == "Member") 
                    ? role 
                    : "Member";
                
                var roleResult = await _userManager.AddToRoleAsync(user, roleToAssign);
                if (!roleResult.Succeeded)
                {
                    TempData["Error"] = $"User created but failed to assign role: {string.Join(", ", roleResult.Errors.Select(e => e.Description))}";
                    return View();
                }

                TempData["Success"] = $"User created successfully with role: {roleToAssign}.";
                return RedirectToAction(nameof(Index));
            }

            foreach (var error in result.Errors)
            {
                ModelState.AddModelError(string.Empty, error.Description);
            }
            return View();
        }

        // POST: Users/Delete/5
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Delete(string id)
        {
            var user = await _userManager.FindByIdAsync(id);
            if (user == null)
            {
                return NotFound();
            }

            var result = await _userManager.DeleteAsync(user);
            if (result.Succeeded)
            {
                TempData["Success"] = "User deleted successfully.";
            }
            else
            {
                TempData["Error"] = "Error deleting user.";
            }

            return RedirectToAction(nameof(Index));
        }
    }
}

