using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using EKnjiznica.Models;

namespace EKnjiznica.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Produces("application/json")]
    public class UsersApiController : ControllerBase
    {
        private readonly UserManager<IdentityUser> _userManager;
        private readonly RoleManager<IdentityRole> _roleManager;

        public UsersApiController(UserManager<IdentityUser> userManager, RoleManager<IdentityRole> roleManager)
        {
            _userManager = userManager;
            _roleManager = roleManager;
        }

        /// <summary>
        /// Get all users (Librarian only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Librarian")]
        [HttpGet]
        public async Task<ActionResult<ApiResponse<List<object>>>> GetAllUsers()
        {
            try
            {
                var members = await _userManager.GetUsersInRoleAsync("Member");
                var librarians = await _userManager.GetUsersInRoleAsync("Librarian");
                var allUsers = members.Concat(librarians).ToList();

                var userList = allUsers.Select(u => new
                {
                    id = u.Id,
                    email = u.Email,
                    roles = _userManager.GetRolesAsync(u).Result.ToList()
                }).ToList();

                return Ok(new ApiResponse<List<object>>
                {
                    Success = true,
                    Data = userList.Cast<object>().ToList(),
                    Message = "Users retrieved successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error retrieving users: {ex.Message}"
                });
            }
        }

        /// <summary>
        /// Get all members (Librarian only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Librarian")]
        [HttpGet("members")]
        public async Task<ActionResult<ApiResponse<List<object>>>> GetMembers()
        {
            try
            {
                var members = await _userManager.GetUsersInRoleAsync("Member");

                var memberList = members.Select(u => new
                {
                    id = u.Id,
                    email = u.Email
                }).ToList();

                return Ok(new ApiResponse<List<object>>
                {
                    Success = true,
                    Data = memberList.Cast<object>().ToList(),
                    Message = "Members retrieved successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error retrieving members: {ex.Message}"
                });
            }
        }

        /// <summary>
        /// Create a new user (Librarian only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Librarian")]
        [HttpPost]
        public async Task<ActionResult<ApiResponse<object>>> CreateUser([FromBody] CreateUserRequest request)
        {
            try
            {
                if (string.IsNullOrEmpty(request.Email) || string.IsNullOrEmpty(request.Password))
                {
                    return BadRequest(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "Email and password are required"
                    });
                }

                var existingUser = await _userManager.FindByEmailAsync(request.Email);
                if (existingUser != null)
                {
                    return BadRequest(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "User with this email already exists"
                    });
                }

                var user = new IdentityUser
                {
                    UserName = request.Email,
                    Email = request.Email,
                    EmailConfirmed = true
                };

                var result = await _userManager.CreateAsync(user, request.Password);
                if (!result.Succeeded)
                {
                    return BadRequest(new ApiResponse<object>
                    {
                        Success = false,
                        Message = string.Join(", ", result.Errors.Select(e => e.Description))
                    });
                }

                // Assign role (default to Member if not specified)
                string roleToAssign = !string.IsNullOrEmpty(request.Role) && 
                    (request.Role == "Librarian" || request.Role == "Member") 
                    ? request.Role 
                    : "Member";

                var roleResult = await _userManager.AddToRoleAsync(user, roleToAssign);
                if (!roleResult.Succeeded)
                {
                    return BadRequest(new ApiResponse<object>
                    {
                        Success = false,
                        Message = $"User created but failed to assign role: {string.Join(", ", roleResult.Errors.Select(e => e.Description))}"
                    });
                }

                return Ok(new ApiResponse<object>
                {
                    Success = true,
                    Data = new { id = user.Id, email = user.Email, role = roleToAssign },
                    Message = $"User created successfully with role: {roleToAssign}"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error creating user: {ex.Message}"
                });
            }
        }

        /// <summary>
        /// Delete a user (Librarian only)
        /// </summary>
        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme, Roles = "Librarian")]
        [HttpDelete("{id}")]
        public async Task<ActionResult<ApiResponse<object>>> DeleteUser(string id)
        {
            try
            {
                var user = await _userManager.FindByIdAsync(id);
                if (user == null)
                {
                    return NotFound(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "User not found"
                    });
                }

                var result = await _userManager.DeleteAsync(user);
                if (!result.Succeeded)
                {
                    return BadRequest(new ApiResponse<object>
                    {
                        Success = false,
                        Message = "Error deleting user"
                    });
                }

                return Ok(new ApiResponse<object>
                {
                    Success = true,
                    Message = "User deleted successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponse<object>
                {
                    Success = false,
                    Message = $"Error deleting user: {ex.Message}"
                });
            }
        }
    }

    public class CreateUserRequest
    {
        public string Email { get; set; } = string.Empty;
        public string Password { get; set; } = string.Empty;
        public string? Role { get; set; }
    }
}
