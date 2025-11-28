using Microsoft.AspNetCore.Identity;

namespace EKnijiznica.Models
{
    public class Reservation
    {
        public int ID { get; set; }

        // Umesto Member koristimo IdentityUser
        public string UserId { get; set; }       // IdentityUser Id (string)
        public IdentityUser User { get; set; }

        public int BookID { get; set; }
        public Book Book { get; set; } = null!;

        public DateTime ReservationDate { get; set; } = DateTime.Now;
        public DateTime ExpiryDate { get; set; }
    }
}
