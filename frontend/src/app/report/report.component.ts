import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-report',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule,],
  templateUrl: './report.component.html',
  styleUrls: ['./report.component.css'],
})
export class ReportComponent {
  startDate: string = '';
  endDate: string = '';
  report: any = null;
  errorMessage: string = '';

  constructor(private http: HttpClient) {}

  generateReport(): void {
    console.log('Generating report for:', this.startDate, this.endDate);
    if (!this.startDate || !this.endDate) {
      this.errorMessage = 'Both start date and end date are required.';
      return;
    }
    const url = `/api/report?startDate=${this.startDate}&endDate=${this.endDate}`;
    this.http.get(url).subscribe({
      next: (data) => {
        console.log('Report data:', data);
        this.report = data;
        this.errorMessage = '';
      },
      error: (err) => {
        console.error('Error generating report:', err);
        this.errorMessage = 'Failed to generate report. Please try again.';
      }
    });
  }
}