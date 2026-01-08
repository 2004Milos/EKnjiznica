using Microsoft.AspNetCore.Identity;
using System.ComponentModel.DataAnnotations;

namespace EKnjiznica.Models
{
    public class Fine
    {
        public int ID { get; set; }
        
        [Required]
        public string UserId { get; set; } = null!;
        public IdentityUser? User { get; set; }
        
        [Required]
        [Range(0.01, double.MaxValue, ErrorMessage = "Amount must be greater than 0")]
        public decimal Amount { get; set; }
        
        [Required]
        [Display(Name = "Reason")]
        public string Reason { get; set; } = null!;
        
        public DateTime IssueDate { get; set; } = DateTime.Now;
        public bool IsPaid { get; set; } = false;
        public DateTime? PaidDate { get; set; }
    }
}

