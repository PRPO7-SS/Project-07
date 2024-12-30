import { Component } from '@angular/core';
import { DataService } from '../services/data.service'; // Import DataService
import { RouterModule, Router } from '@angular/router';
import { RegisterRequest } from '../models/registerRequest';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ FormsModule, RouterModule, CommonModule ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  supervisionCode: string = '';
  showUnderageMessage: boolean = false; // Tracks if the underage message should be displayed
  message: string | null = null; // Success or error message
  loading: boolean = false; // Loading state for the register button

  user = {
      fullName: '',
      email: '',
      username: '',
      password: '',
      confirmPassword: '',
      dob: '',
    };

   missingFields: { [key: string]: boolean } = {};

   constructor(private dataService: DataService, private router: Router) {}


  registerUser() {
      // Reset missingFields object
      this.missingFields = {};

      // Check for empty required fields
      Object.entries(this.user).forEach(([key, value]) => {
        if (value.trim() === '') {
          this.missingFields[key] = true; // Mark field as missing
        }
      });

      // Validate missing fields
      if (Object.keys(this.missingFields).length > 0) {
        this.message = 'Please fill all required fields.';
        return;
      }

      // Validate email format
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(this.user.email)) {
        this.message = 'Invalid email';
        return;
      }

      // Validate password length
      if (this.user.password.length < 8) {
        this.message = 'Password must be at least 8 characters long.';
        return;
      }

      // Validate passwords match
      if (this.user.password !== this.user.confirmPassword) {
        this.message = ('Passwords do not match.');
        return;
      }

      // If all validations pass, clear the error message
      this.message = null;

      // Validate date of birth (must be at least 18 years old)
      const dob = new Date(this.user.dob);
      const today = new Date();
      const age = today.getFullYear() - dob.getFullYear();
      const monthDiff = today.getMonth() - dob.getMonth();
      const dayDiff = today.getDate() - dob.getDate();

      const isUnderage =
        age < 18 || (age === 18 && (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)));

      if (isUnderage ) {
        this.showUnderageMessage = true; // Show the dynamic message in the template
        return;
      } else {
        this.showUnderageMessage = false;
      }

      // Create a payload object to send to the backend
      const payload: RegisterRequest = {
        fullName: this.user.fullName,
        email: this.user.email,
        username: this.user.username,
        password: this.user.password,
        dateOfBirth: this.user.dob,
      };

      // Show loading state
      this.loading = true;

      // Make the API call to register using DataService
      this.dataService.register(payload).subscribe({
        next: () => {
          this.resetForm();
          this.router.navigate(['/home']);
        },
        error: (error) => {
          this.message = error.error?.message || 'Registration failed.';
        },
        complete: () => {
          this.loading = false;
        },
      });
    }

    resetForm() {
        this.user = {
          fullName: '',
          email: '',
          username: '',
          password: '',
          confirmPassword: '',
          dob: '',
        };
     }

}
