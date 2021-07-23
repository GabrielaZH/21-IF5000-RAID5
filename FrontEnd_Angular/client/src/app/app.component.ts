import { Component } from '@angular/core';
import { Book } from './models/book.model';
import { Observable } from "rxjs";
import { ClientService } from './services/client.service';
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
  file: File;
  nodes: string;
  public bookList: Observable<Book[]>;
  dtOptions: DataTables.Settings = {};
  placeholder = 'número de nodos'
  constructor(private clientService: ClientService, private fb: FormBuilder) {
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
  selectFile(event: any) {
    this.file = event.target.files.item(0);
  }
  onKey(event: any) {
    const inputValue = event.target.value;
    this.nodes = inputValue;
  }


  upload() {
    this.clientService.upload(this.file, this.nodes).subscribe((res) => {
      var time = 0;
      var nodesString = this.nodes;
      var nodesNumber: number = +nodesString;

      if (nodesNumber <= 8) {
        time = 5000;
      } else if (nodesNumber <= 16) {
        time = 10000;
      }

      let timerInterval: any
      Swal.fire({
        title: 'Subiendo...',
        timer: time * nodesNumber,
        timerProgressBar: true,
        didOpen: () => {
          Swal.showLoading()
          timerInterval = setInterval(() => {
            const content = Swal.getHtmlContainer()
            if (content) {
              const b = content.querySelector('b')
              if (b) {
                b.textContent = Swal.getTimerLeft() + ""
              }
            }
          }, 100)
        },
        willClose: () => {
          clearInterval(timerInterval)
        }
      }).then((result) => {
        if (result.dismiss === Swal.DismissReason.timer) {
          if (res) {
            Swal.fire({
              title: 'Subido',
              icon: 'success'
            });
            this.getBooks();
          } else {
            Swal.fire({
              title: 'Perdon, intentalo de nuevo!',
              icon: 'error'
            });
          }
        }
      })
    });
  }

  request(name: any, numNodes: any) {
    this.showLoading();
    console.log(numNodes, name)
    this.clientService.getBook(name, numNodes).subscribe(blob => saveAs(blob, name));
  }

  showLoading() {
    let timerInterval: any
    Swal.fire({
      title: 'Descargando...',
      timer: 10000,
      timerProgressBar: true,
      didOpen: () => {
        Swal.showLoading()
        timerInterval = setInterval(() => {
          const content = Swal.getHtmlContainer()
          if (content) {
            const b = content.querySelector('b')
            if (b) {
              b.textContent = Swal.getTimerLeft() + ""
            }
          }
        }, 100)
      },
      willClose: () => {
        clearInterval(timerInterval)
      }
    })
  };

}
