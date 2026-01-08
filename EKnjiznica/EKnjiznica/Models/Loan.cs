using Microsoft.AspNetCore.Identity;

namespace EKnjiznica.Models
{
    public class Loan
    {
        public int ID { get; set; }

        // Umesto Member koristimo IdentityUser
        public string UserId { get; set; }       // IdentityUser Id (string)
        public IdentityUser User { get; set; }

        public int BookID { get; set; }
        public Book Book { get; set; } = null!;

        public DateTime LoanDate { get; set; } = DateTime.Now;
        public DateTime DueDate { get; set; }
        public DateTime? ReturnDate { get; set; }

        public string Status { get; set; } = "Active"; // Active, Overdue, Returned
    }
}
