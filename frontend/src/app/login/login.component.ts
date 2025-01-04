import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { DataService } from '../services/data.service';
import { NavigationComponent } from '../navigation/navigation.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, NavigationComponent],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  email: string = '';
  password: string = '';
  errorMessage: string = '';

  formData = {
      email: '',
      password: ''
   };

 message: string | null = null; // Error message
 missingFields: { [key: string]: boolean } = {}; // Tracks missing fields
 loading: boolean = false; // Loading state
 returnUrl: string = '/home'; // Default return URL

  constructor(private dataService: DataService, private router: Router) {}

  onLogin() {
    this.missingFields = {}; // Reset missing fields

    // Check for missing fields
    if (!this.formData.email.trim()) {
      this.missingFields['email'] = true;
    }
    if (!this.formData.password.trim()) {
      this.missingFields['password'] = true;
    }

    if (Object.keys(this.missingFields).length > 0) {
      this.message = 'Fill required fields';
      return;
    }

    // Clear message
    this.message = null;

    this.loading = true;
    this.dataService.login(this.formData).subscribe({
          next: (response: any) => {
            this.loading = false;
            this.router.navigateByUrl(this.returnUrl);
          },
          error: (error: { error: { message: string }; status: number }) => {
            this.loading = false;
            this.message = error.error?.message || 'Invalid credentials.';
          },
     });
  }
}
