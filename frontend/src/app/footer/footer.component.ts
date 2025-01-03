import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule , Router} from '@angular/router';
import { FormsModule } from '@angular/forms';


@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css']
})
export class FooterComponent {
  @Input() showFullFooter: boolean = true;
  startDate: string = '';
  endDate: string = '';

  constructor(
    private readonly router: Router
  ) {}



  goToReport(): void {
    if (this.startDate && this.endDate) {
      // Construct the URL with query parameters
      const reportUrl = this.router.createUrlTree(['/reports'], {
        queryParams: { startDate: this.startDate, endDate: this.endDate },
      }).toString();
      
      // Open the URL in a new tab
      window.open(reportUrl, '_blank');
    } else {
      alert('Please select both start and end dates.');
    }
  }
  
}