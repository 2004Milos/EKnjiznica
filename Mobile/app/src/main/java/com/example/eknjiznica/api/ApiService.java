package com.example.eknjiznica.api;

import com.example.eknjiznica.models.ApiResponse;
import com.example.eknjiznica.models.Book;
import com.example.eknjiznica.models.CreateLoanRequest;
import com.example.eknjiznica.models.Loan;
import com.example.eknjiznica.models.LoginRequest;
import com.example.eknjiznica.models.LoginResponse;
import com.example.eknjiznica.models.RegisterRequest;
import com.example.eknjiznica.models.Reservation;

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

    @POST("api/AuthApi/register")
    Call<LoginResponse> register(@Body RegisterRequest request);

    // Books endpoints
    @GET("api/BooksApi")
    Call<ApiResponse<List<Book>>> getBooks();

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
}
