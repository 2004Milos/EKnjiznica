namespace EKnijiznica.Models
{
    public class Book
    {
        public int ID { get; set; }
        public string Title { get; set; } = null!;
        public string Author { get; set; } = null!;
        public string Genre { get; set; } = null!;
        public int YearPublished { get; set; }
        public bool IsAvailable { get; set; } = true;
    }
}
