import { Component } from '@angular/core';
import { Book } from './models/book.model';
import {Observable} from "rxjs";
import {ClientService} from './services/client.service';
import Swal from 'sweetalert2';
import { FormBuilder, FormGroup } from "@angular/forms";
import { saveAs } from 'file-saver';
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'RAID';
  file:File;
  nodes:string;
  public bookList: Observable<Book[]>;
  dtOptions: DataTables.Settings = {};
  placeholder='número de nodos'
  constructor(private clientService: ClientService,private fb:FormBuilder) {
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
  onKey(event: any) {
    const inputValue = event.target.value;
    this.nodes=inputValue;
  }


  upload() {
    this.showLoading();
    this.clientService.upload(this.file,this.nodes).subscribe((result) => {
      if(result){
        Swal.fire({
          title: 'Uploaded',
          icon: 'success'
        });
        this.getBooks();
        return true;
      }else{
        Swal.fire({
          title: 'Sorry, try again!',
          icon: 'error'
        });
        return false;
      }
    });
  }


  request(name: any, numNodes:any ) {
    console.log(numNodes,name)
    this.clientService.getBook(name,numNodes).subscribe(blob => saveAs(blob, name));
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
