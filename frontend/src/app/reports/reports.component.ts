import {
  Component,
  AfterViewInit,
  ViewChild,
  ElementRef,
  Inject,
  PLATFORM_ID,
  ChangeDetectorRef,
} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Chart, ChartConfiguration } from 'chart.js/auto';
import { ActivatedRoute } from '@angular/router';
import { DataService } from '../core/services/data.service';
import { TranslateService, TranslateModule } from '@ngx-translate/core';
import { ReportResponse } from '../shared/models/reportResponse';
import { FooterComponent } from '../footer/footer.component';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, TranslateModule, FooterComponent],
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css'],
})
export class ReportsComponent implements AfterViewInit {
  @ViewChild('spendingChart', { static: false }) spendingChart!: ElementRef<HTMLCanvasElement>;
  @ViewChild('spendingDateChart', { static: false }) spendingDateChart!: ElementRef<HTMLCanvasElement>;

  report!: ReportResponse;
  loading: boolean = true;
  private readonly isBrowser: boolean;

  constructor(
    @Inject(PLATFORM_ID) private readonly platformId: Object,
    private readonly route: ActivatedRoute,
    private readonly dataService: DataService,
    private readonly translate: TranslateService,
    private readonly cdRef: ChangeDetectorRef
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
    this.translate.setDefaultLang('en');
  }

  ngAfterViewInit(): void {
    if (this.isBrowser) {
      this.loadReport();
    }
  }

  private loadReport(): void {
    this.route.paramMap.subscribe((params) => {
      const reportId = params.get('reportId');
      if (reportId) {
        this.fetchReportById(reportId);
      } else {
        this.route.queryParamMap.subscribe((queryParams) => {
          const startDate = queryParams.get('startDate');
          const endDate = queryParams.get('endDate');
          if (startDate && endDate) {
            this.generateCustomReport(startDate, endDate);
          } else {
            this.translate
              .get('REPORT_PAGE.ERROR_INVALID_PARAMS')
              .subscribe((message) => console.error(message));
          }
        });
      }
    });
  }

  private fetchReportById(reportId: string): void {
    this.dataService.getMonthlyReport(reportId).subscribe({
      next: (data: ReportResponse) => {
        this.report = data;
        this.loading = false;
        // Ensure the view is updated so that ViewChild references exist
        this.cdRef.detectChanges();
        this.renderCharts();
      },
      error: () =>
        this.translate
          .get('REPORT_PAGE.ERROR_FETCHING_REPORT_BY_ID')
          .subscribe((message) => console.error(message)),
    });
  }

  private generateCustomReport(startDate: string, endDate: string): void {
    this.dataService.generateCustomReport({ startDate, endDate }).subscribe({
      next: (data: ReportResponse) => {
        this.report = data;
        console.log(data);
        this.loading = false;
        // Ensure the view is updated so that ViewChild references exist
        this.cdRef.detectChanges();
        this.renderCharts();
      },
      error: () =>
        this.translate
          .get('REPORT_PAGE.ERROR_GENERATING_CUSTOM_REPORT')
          .subscribe((message) => console.error(message)),
    });
  }

  private renderCharts(): void {
    if (!this.report.transactionSummary) {
      return; // No transaction summary available
    }

    const { spendingByCategory, spendingByDate } = this.report.transactionSummary;

    if (spendingByCategory && this.spendingChart?.nativeElement) {
      this.renderCategoryChart(spendingByCategory);
    }

    if (spendingByDate && this.spendingDateChart?.nativeElement) {
      this.renderDateChart(spendingByDate);
    }
  }

  private renderCategoryChart(spendingByCategory: { [category: string]: number }): void {
    const categoryLabels = Object.keys(spendingByCategory).map((key) =>
      this.translate.instant('CATEGORIES.' + key.toUpperCase())
    );

    const categoryConfig: ChartConfiguration<'pie', number[], string> = {
      type: 'pie',
      data: {
        labels: categoryLabels,
        datasets: [
          {
            data: Object.values(spendingByCategory),
            backgroundColor: Object.keys(spendingByCategory).map(
              (category) => this.getCategoryColor(category.toLowerCase())
            ),
          },
        ],
      },
      options: {
        responsive: false,
        plugins: {
          tooltip: {
            callbacks: {
              label: (context) =>
                `${context.label}: ${this.translate.instant('REPORT_PAGE.AMOUNT', {
                  amount: context.raw,
                })}`,
            },
          },
        },
      },
    };
    new Chart(this.spendingChart.nativeElement, categoryConfig);
  }

  private renderDateChart(spendingByDate: { [date: string]: { [category: string]: number } }): void {
    const uniqueCategories = Array.from(
      new Set(
        Object.values(spendingByDate).flatMap((dateEntry) => Object.keys(dateEntry))
      )
    );

    const datasets = uniqueCategories.map((category) => ({
      label: this.translate.instant('CATEGORIES.' + category.toUpperCase()),
      data: Object.keys(spendingByDate).map((date) => spendingByDate[date]?.[category] || 0),
      backgroundColor: this.getCategoryColor(category.toLowerCase()),
    }));

    const dateConfig: ChartConfiguration<'bar', number[], string> = {
      type: 'bar',
      data: {
        labels: Object.keys(spendingByDate),
        datasets: datasets,
      },
      options: {
        responsive: true,
        plugins: {
          tooltip: {
            callbacks: {
              label: (context) =>
                `${context.dataset.label}: ${this.translate.instant('REPORT_PAGE.AMOUNT', {
                  amount: context.raw,
                })}`,
            },
          },
        },
        scales: {
          x: {
            title: { display: true, text: this.translate.instant('REPORT_PAGE.DATE') },
            stacked: true,
          },
          y: {
            title: { display: true, text: this.translate.instant('REPORT_PAGE.AMOUNT_LABEL') },
            stacked: true,
          },
        },
      },
    };

    new Chart(this.spendingDateChart.nativeElement, dateConfig);
  }

  private getCategoryColor(category: string): string {
    const colors: { [key: string]: string } = {
      salary: '#28a745',
      scholarship: '#17a2b8',
      gifts: '#ffc107',
      other: '#fd7e14',
      groceries: '#fd7e14',
      clothes: '#B57A49',
      school: '#ffc107',
      transportation: '#17a2b8',
      subscriptions: '#228B22',
      subscription: '#228B22',
      eating: '#ad67af',
      health: '#FF0F3F',
      selfcare: '#483b6d',
    };
    return colors[category] || '#000000';
  }
}
