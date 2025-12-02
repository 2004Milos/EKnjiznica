using Microsoft.AspNetCore.Identity;
using System.ComponentModel.DataAnnotations;

namespace EKnjiznica.Models
{
    public class Review
    {
        public int ID { get; set; }
        
        public int BookID { get; set; }
        public Book Book { get; set; } = null!;
        
        public string UserId { get; set; }
        public IdentityUser User { get; set; }
        
        [Required]
        [Range(1, 5, ErrorMessage = "Rating must be between 1 and 5")]
        public int Rating { get; set; }
        
        public string? Comment { get; set; }
        public DateTime ReviewDate { get; set; } = DateTime.Now;
    }
}

