import { Component } from '@angular/core';
import { Book } from './models/book.model';
import {Observable} from "rxjs";
import {ClientService} from './services/client.service';
import Swal from 'sweetalert2';
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'RAID';
  file:File;
  public bookList: Observable<Book[]>;
  dtOptions: DataTables.Settings = {};


  constructor(private clientService: ClientService) {

  }

  ngOnInit() {
    this.getBooks();
    this.dtOptions = {
      pagingType: 'full_numbers'
    };

  }

  getBooks() {
    this.bookList = this.clientService.getBooks();
  }
  selectFile(event:any) {
    this.file = event.target.files.item(0);
  }


  upload() {
    this.showLoading();
    this.clientService.upload(this.file).subscribe((result) => {
      if(result){
        Swal.fire({
          title: 'Uploaded',
          icon: 'success',
          timer: 1000,
        });
        return true;
      }else{
        Swal.fire({
          title: 'Sorry, try again!',
          icon: 'error',
          timer: 1000,
        });
        return false;
      }
    });
  }

  showLoading(){
    Swal.fire({
        title: 'Uploading...',
        allowEscapeKey: false,
        allowOutsideClick: false,
        timer: 100000,
        onOpen: () => {
          Swal.showLoading();
        }
    })
  };

}
