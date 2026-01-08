using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

#pragma warning disable CA1814 // Prefer jagged arrays over multidimensional

namespace EKnjiznica.Migrations
{
    /// <inheritdoc />
    public partial class InitialCreate1 : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DeleteData(
                table: "AspNetRoles",
                keyColumn: "Id",
                keyValue: "27902f87-37b3-4242-8ca7-6ca036e73875");

            migrationBuilder.DeleteData(
                table: "AspNetRoles",
                keyColumn: "Id",
                keyValue: "98c714eb-d843-4522-97cd-80a10aa08037");

            migrationBuilder.InsertData(
                table: "AspNetRoles",
                columns: new[] { "Id", "ConcurrencyStamp", "Name", "NormalizedName" },
                values: new object[,]
                {
                    { "d779e2d1-9fca-49ed-82ac-5c276394c3fd", null, "Member", "MEMBER" },
                    { "f6aa1cf2-42a5-4da8-b6ea-2ca281e36876", null, "Librarian", "LIBRARIAN" }
                });
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DeleteData(
                table: "AspNetRoles",
                keyColumn: "Id",
                keyValue: "d779e2d1-9fca-49ed-82ac-5c276394c3fd");

            migrationBuilder.DeleteData(
                table: "AspNetRoles",
                keyColumn: "Id",
                keyValue: "f6aa1cf2-42a5-4da8-b6ea-2ca281e36876");

            migrationBuilder.InsertData(
                table: "AspNetRoles",
                columns: new[] { "Id", "ConcurrencyStamp", "Name", "NormalizedName" },
                values: new object[,]
                {
                    { "27902f87-37b3-4242-8ca7-6ca036e73875", null, "Librarian", "LIBRARIAN" },
                    { "98c714eb-d843-4522-97cd-80a10aa08037", null, "Member", "MEMBER" }
                });
        }
    }
}
