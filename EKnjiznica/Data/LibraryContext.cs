using EKnjiznica.Models;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Identity.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore;

namespace EKnjiznica.Data
{
    public class LibraryContext : IdentityDbContext<IdentityUser>
    {
        public LibraryContext(DbContextOptions<LibraryContext> options)
            : base(options)
        {
        }

        public DbSet<Book> Books { get; set; }
        public DbSet<Loan> Loans { get; set; }
        public DbSet<Reservation> Reservations { get; set; }
        public DbSet<Fine> Fines { get; set; }
        public DbSet<Review> Reviews { get; set; }


        protected override void OnModelCreating(ModelBuilder builder)
        {
            base.OnModelCreating(builder);

            // Configure Book entity
            builder.Entity<Book>().ToTable("Book");
            builder.Entity<Book>().Property(b => b.Id).HasColumnName("ID");
            builder.Entity<Book>().Property(b => b.Year).HasColumnName("YearPublished");

            builder.Entity<Loan>().ToTable("Loan");
            builder.Entity<Reservation>().ToTable("Reservation");
            builder.Entity<Fine>().ToTable("Fine");
            builder.Entity<Review>().ToTable("Review");

            // Seed roles
            builder.Entity<IdentityRole>().HasData(
                new IdentityRole { Name = "Librarian", NormalizedName = "LIBRARIAN" },
                new IdentityRole { Name = "Member", NormalizedName = "MEMBER" }
            );
        }

    }
}
