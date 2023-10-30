import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DepartureService } from '../departure.service';
import { DepartureDetailsModel } from 'src/shared/departure';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PlaneModel } from 'src/shared/plane';
import { PlaneService } from '../plane.service';

@Component({
  selector: 'app-departure-page',
  templateUrl: './departure-page.component.html',
  styleUrls: ['./departure-page.component.css']
})
export class DeparturePageComponent implements OnInit {
  createForm!: FormGroup;
  planes: PlaneModel[] = [];
  date!: string;
  createDeparture: boolean = false;
  departures: DepartureDetailsModel[] = [];
  canCreate: boolean = false;
  rolesAllowedToCreate = ['ADMIN', 'MANIFEST'];

  constructor(private route: ActivatedRoute, private departureService: DepartureService,  private formBuilder: FormBuilder, private planeService: PlaneService){
    this.route.queryParams.subscribe(params => {this.date = params['date'] as string; console.log(params['date'] as string);});
    this.createForm = formBuilder.group({
      date: [this.date, [Validators.required]],
      time: ['', [Validators.required]],
      allowStudents: ['', ],
      allowAFF: ['', ],
      planeId: ['', [Validators.required]]
    })
  }
  ngOnInit(): void {
    this.departureService.getDeparturesDetails(this.date).subscribe(data => {
      this.departures = data as DepartureDetailsModel[];
    });
    if(this.rolesAllowedToCreate.indexOf(window.localStorage.getItem('role') as string) !== -1) {
      this.canCreate = true;
    }
  }

  deleteDeparture(departure: DepartureDetailsModel){
    this.departures = this.departures.filter(data => data.id != departure.id);
  }

  showForm(){
    this.planeService.getAllPlanes().subscribe(plane => this.planes = plane as PlaneModel[]);
    this.createDeparture = true;
  }

  cancel(){
    this.createForm.reset();
    this.createDeparture = false;
  }

  submit(){
    console.log('submit');
    if(this.createForm.valid){
      console.log('validating');
      this.departureService.createDeparture(this.createForm.value).subscribe(dep => this.departures.push(dep as DepartureDetailsModel));
    }
    this.cancel();
    
  }

}
