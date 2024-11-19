import { Routes } from '@angular/router';
import { FinanceComponent } from './pages/finance/finance.component';
import { InvestmentsComponent } from './pages/investments/investments.component';
import { HomeComponent } from './pages/home/home.component';
import { RegisterComponent } from './pages/register/register.component';
import { LoginComponent } from './pages/login/login.component';
import { SettingsComponent } from './pages/settings/settings.component';

export const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' }, // Preusmeri na /home
  { path: 'home', component: HomeComponent },
  { path: 'finance', component: FinanceComponent },
  { path: 'investments', component: InvestmentsComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'login', component: LoginComponent },
  { path: 'settings', component: SettingsComponent },
  { path: '**', redirectTo: '/home' }, // Preusmeri na /home za vse neveljavne poti
];