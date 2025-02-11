import { Routes} from '@angular/router';
import { LoginComponent } from './login/login.component';
import { HomepageComponent } from './homepage/homepage.component';
import { RegisterComponent } from './register/register.component';
import { SettingsComponent } from './settings/settings.component';
import { LandingPageComponent  } from './landing-page/landing-page.component';
import { FinanceComponent } from './finance/finance.component';
import { InvestmentsComponent  } from './investments/investments.component';
import { authGuard } from './guards/auth.guard';
import { BudgetsComponent } from './budgets/budgets.component';
import { DebtsComponent } from './debts/debts.component';


export const routes: Routes = [
  { path: 'login', component: LoginComponent },
   { path: 'home', component: HomepageComponent },
   { path: 'register', component: RegisterComponent },
   { path: 'settings', component: SettingsComponent, canActivate: [authGuard] },
   { path: 'finance', component: FinanceComponent, canActivate: [authGuard] },
   { path: 'investments', component: InvestmentsComponent, canActivate: [authGuard] },
   { path: 'budgets', component: BudgetsComponent, canActivate: [authGuard] },
   { path: 'debts', component: DebtsComponent, canActivate: [authGuard] },
   { path: '', component: LandingPageComponent },
   { path: '**', redirectTo: 'home' },
];
