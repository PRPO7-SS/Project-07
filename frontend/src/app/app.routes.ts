import { Routes} from '@angular/router';
import { LoginComponent } from './login/login.component';
import { HomepageComponent } from './homepage/homepage.component';
import { RegisterComponent } from './register/register.component';
import { SettingsComponent } from './settings/settings.component';
import { LandingPageComponent  } from './landing-page/landing-page.component';
import { FinanceComponent } from './finance/finance.component';
import { ReportsComponent } from './reports/reports.component';
import { InvestmentsComponent  } from './investments/investments.component';


export const routes: Routes = [
  { path: 'login', component: LoginComponent },
   { path: 'home', component: HomepageComponent },
   { path: 'register', component: RegisterComponent },
   { path: 'settings', component: SettingsComponent },
   { path: 'finance', component: FinanceComponent },
   { path: 'report', component: ReportsComponent },
   { path: 'investments', component: InvestmentsComponent },
   { path: '', component: LandingPageComponent },
   { path: '**', redirectTo: 'home' },
];
