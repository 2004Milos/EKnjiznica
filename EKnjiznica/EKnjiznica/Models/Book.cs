using System.ComponentModel.DataAnnotations;

namespace EKnjiznica.Models
{
    public class Book
    {
        public int Id { get; set; }
        
        [Required]
        [Display(Name = "Title")]
        public string Title { get; set; } = null!;
        
        [Required]
        [Display(Name = "Author")]
        public string Author { get; set; } = string.Empty;
        
        [Required]
        [Range(1000, 2100, ErrorMessage = "Year must be between 1000 and 2100")]
        [Display(Name = "Year")]
        public int Year { get; set; }
        
        [Required]
        [Display(Name = "Genre")]
        public string Genre { get; set; } = null!;
        
        [Display(Name = "Available")]
        public bool IsAvailable { get; set; } = true;
    }
}
