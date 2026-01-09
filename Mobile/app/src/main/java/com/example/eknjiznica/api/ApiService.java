package com.example.eknjiznica.api;

import com.example.eknjiznica.models.ApiResponse;
import com.example.eknjiznica.models.Book;
import com.example.eknjiznica.models.CreateFineRequest;
import com.example.eknjiznica.models.CreateLoanRequest;
import com.example.eknjiznica.models.CreateReviewRequest;
import com.example.eknjiznica.models.CreateUserRequest;
import com.example.eknjiznica.models.Fine;
import com.example.eknjiznica.models.Loan;
import com.example.eknjiznica.models.LoginRequest;
import com.example.eknjiznica.models.LoginResponse;
import com.example.eknjiznica.models.Reservation;
import com.example.eknjiznica.models.Review;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    // Auth endpoints
    @POST("api/AuthApi/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // Books endpoints
    @GET("api/BooksApi")
    Call<ApiResponse<List<Book>>> getBooks(@retrofit2.http.Query("search") String search);

    @GET("api/BooksApi/{id}")
    Call<ApiResponse<Book>> getBook(@Path("id") int id);

    @POST("api/BooksApi")
    Call<ApiResponse<Book>> addBook(@Header("Authorization") String token, @Body Book book);

    @PUT("api/BooksApi/{id}")
    Call<ApiResponse<Book>> updateBook(@Header("Authorization") String token, @Path("id") int id, @Body Book book);

    @DELETE("api/BooksApi/{id}")
    Call<ApiResponse<Object>> deleteBook(@Header("Authorization") String token, @Path("id") int id);

    // Loans endpoints
    @GET("api/LoansApi")
    Call<ApiResponse<List<Loan>>> getAllLoans(@Header("Authorization") String token);

    @GET("api/LoansApi/my")
    Call<ApiResponse<List<Loan>>> getMyLoans(@Header("Authorization") String token);

    @POST("api/LoansApi/create")
    Call<ApiResponse<Loan>> createLoan(@Header("Authorization") String token, @Body CreateLoanRequest request);

    @POST("api/LoansApi/return/{loanId}")
    Call<ApiResponse<Loan>> returnLoan(@Header("Authorization") String token, @Path("loanId") int loanId);

    // Reservations endpoints
    @POST("api/ReservationsApi/{bookId}")
    Call<ApiResponse<Reservation>> reserveBook(@Header("Authorization") String token, @Path("bookId") int bookId);

    @GET("api/ReservationsApi")
    Call<ApiResponse<List<Reservation>>> getAllReservations(@Header("Authorization") String token);

    @GET("api/ReservationsApi/my")
    Call<ApiResponse<List<Reservation>>> getMyReservations(@Header("Authorization") String token);

    @POST("api/ReservationsApi/approve/{reservationId}")
    Call<ApiResponse<Loan>> approveReservation(@Header("Authorization") String token, @Path("reservationId") int reservationId);

    // Reviews endpoints
    @GET("api/ReviewsApi/book/{bookId}")
    Call<ApiResponse<List<Review>>> getBookReviews(@Path("bookId") int bookId);

    @GET("api/ReviewsApi/book/{bookId}/rating")
    Call<ApiResponse<Object>> getBookRating(@Path("bookId") int bookId);

    @POST("api/ReviewsApi")
    Call<ApiResponse<Review>> createReview(@Header("Authorization") String token, @Body CreateReviewRequest request);

    @DELETE("api/ReviewsApi/{id}")
    Call<ApiResponse<Object>> deleteReview(@Header("Authorization") String token, @Path("id") int id);

    @GET("api/ReviewsApi/my")
    Call<ApiResponse<List<Review>>> getMyReviews(@Header("Authorization") String token);

    // Fines endpoints
    @GET("api/FinesApi")
    Call<ApiResponse<List<Fine>>> getAllFines(@Header("Authorization") String token);

    @GET("api/FinesApi/my")
    Call<ApiResponse<List<Fine>>> getMyFines(@Header("Authorization") String token);

    @POST("api/FinesApi")
    Call<ApiResponse<Fine>> createFine(@Header("Authorization") String token, @Body CreateFineRequest request);

    @POST("api/FinesApi/{id}/mark-paid")
    Call<ApiResponse<Fine>> markFineAsPaid(@Header("Authorization") String token, @Path("id") int id);

    // Users endpoints
    @GET("api/UsersApi")
    Call<ApiResponse<List<Object>>> getAllUsers(@Header("Authorization") String token);

    @GET("api/UsersApi/members")
    Call<ApiResponse<List<Object>>> getMembers(@Header("Authorization") String token);

    @POST("api/UsersApi")
    Call<ApiResponse<Object>> createUser(@Header("Authorization") String token, @Body CreateUserRequest request);

    @DELETE("api/UsersApi/{id}")
    Call<ApiResponse<Object>> deleteUser(@Header("Authorization") String token, @Path("id") String id);

    // Statistics endpoints
    @GET("api/StatisticsApi/librarian")
    Call<ApiResponse<Object>> getLibrarianStatistics(@Header("Authorization") String token);

    @GET("api/StatisticsApi/member")
    Call<ApiResponse<Object>> getMemberStatistics(@Header("Authorization") String token);
}
