import { Routes} from '@angular/router';
import { LoginComponent } from './login/login.component';
import { HomepageComponent } from './homepage/homepage.component';
import { RegisterComponent } from './register/register.component';
import { SettingsComponent } from './settings/settings.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
   { path: 'home', component: HomepageComponent },
   { path: 'register', component: RegisterComponent },
   { path: 'settings', component: SettingsComponent },
   { path: '', component: HomepageComponent },
   { path: '**', redirectTo: 'home' },
];
