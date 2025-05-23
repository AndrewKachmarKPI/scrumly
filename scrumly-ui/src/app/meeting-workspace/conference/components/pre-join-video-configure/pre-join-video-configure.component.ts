import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { catchError, from } from 'rxjs';

export interface DeviceConfig {
  label: string;
  value: string
}

export interface DeviceSetup {
  name: string,
  camera: DeviceConfig,
  microphone: DeviceConfig,
  cameraOn: boolean,
  micOn: boolean,
}

@Component({
  selector: 'pre-join-video-configure',
  templateUrl: './pre-join-video-configure.component.html',
  styleUrl: './pre-join-video-configure.component.css'
})
export class PreJoinVideoConfigureComponent implements OnInit, OnDestroy {
  @ViewChild('videoElement', { static: false }) videoElement!: ElementRef<HTMLVideoElement>;

  userName: string = '';
  isCameraOn: boolean = true;
  isMicrophoneOn: boolean = true;
  cameras: DeviceConfig[] = [];
  microphones: DeviceConfig[] = [];
  selectedCamera?: DeviceConfig;
  selectedMicrophone?: DeviceConfig;
  stream?: MediaStream;


  ngOnInit() {
    from(navigator.mediaDevices.getUserMedia({ video: true, audio: true }))
      .subscribe({
        next: () => {
          this.getDevices();
        }
      })
  }

  ngOnDestroy() {
    this.stopVideo();
  }


  getDevices() {
    from(navigator.mediaDevices.enumerateDevices())
      .pipe(
        catchError(error => {
          console.error('Error getting devices:', error);
          return []; // Return an empty array if an error occurs
        })
      )
      .subscribe(devices => {
        this.cameras = devices
          .filter(device => device.kind === 'videoinput')
          .map(device => ({ label: device.label, value: device.deviceId }));

        this.microphones = devices
          .filter(device => device.kind === 'audioinput')
          .map(device => ({ label: device.label, value: device.deviceId }));

        if (this.cameras.length > 0) {
          this.selectedCamera = this.cameras[0];
          this.startVideo();
        }

        if (this.microphones.length > 0) {
          this.selectedMicrophone = this.microphones[0];
        }
      });
  }

  startVideo() {
    if (!this.selectedCamera) {
      console.error('No camera selected');
      return;
    }

    from(navigator.mediaDevices.getUserMedia({
      video: { deviceId: this.selectedCamera ? { ideal: this.selectedCamera.value } : undefined },
      audio: this.selectedMicrophone ? { deviceId: { ideal: this.selectedMicrophone.value } } : undefined
    }))
      .pipe(
        catchError(error => {
          console.error('Error accessing camera:', error);
          return [];
        })
      )
      .subscribe(stream => {
        this.stream = stream;
        if (this.videoElement?.nativeElement) {
          this.videoElement.nativeElement.srcObject = this.stream;
        }
      });
  }

  stopVideo() {
    if (this.stream) {
      this.stream.getTracks().forEach(track => track.stop());
    }
  }

  changeCamera() {
    if (this.stream) {
      this.stream.getTracks().forEach(track => track.stop());
    }

    from(navigator.mediaDevices.getUserMedia({
      video: { deviceId: this.selectedCamera ? { ideal: this.selectedCamera.value } : undefined },
      audio: this.selectedMicrophone ? { deviceId: { ideal: this.selectedMicrophone.value } } : undefined
    }))
      .pipe(
        catchError(error => {
          console.error('Error switching camera:', error);
          return []; // Return an empty stream if an error occurs
        })
      )
      .subscribe(stream => {
        this.stream = stream;
        if (this.videoElement?.nativeElement) {
          this.videoElement.nativeElement.srcObject = this.stream;
        }
      });
  }

  toggleCamera() {
    this.isCameraOn = !this.isCameraOn;
    this.stream?.getVideoTracks().forEach(track => (track.enabled = this.isCameraOn));
  }

  toggleMicrophone() {
    this.isMicrophoneOn = !this.isMicrophoneOn;
    this.stream?.getAudioTracks().forEach(track => (track.enabled = this.isMicrophoneOn));
  }

  getDeviceSetup(): DeviceSetup {
    return {
      name: this.userName,
      camera: this.selectedCamera!,
      microphone: this.selectedMicrophone!,
      cameraOn: this.isCameraOn,
      micOn: this.isMicrophoneOn,
    };
  }
}
