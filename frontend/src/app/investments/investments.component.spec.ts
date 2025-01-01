import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InvestmensComponent } from './investmens.component';

describe('InvestmensComponent', () => {
  let component: InvestmensComponent;
  let fixture: ComponentFixture<InvestmensComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InvestmensComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InvestmensComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
