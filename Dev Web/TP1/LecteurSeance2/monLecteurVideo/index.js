import './lib/webaudio-controls.js';

const ctx = new (window.AudioContext || window.webkitAudioContext);
const audioContext = ctx;

const getBaseURL = () => {
    return new URL('.', import.meta.url);
};

let style = `

.main-container {
}

.buttonContainer {
    margin-top: 5%;
    text-align: center;
    float: left;
    width: 25%;
}

#player:hover {
    background-color: #006CC9;
}

.playerClicked {
    background-color: #006CC9;
}

.pauseClicked {
    background-color: #006CC9;
}

.vitesse4Clicked{
    background-color: #006CC9;
}

#vitesse4:hover {
    background-color: #006CC9;
}

#avance10:hover {
    background-color: #006CC9;
}

.videoContainer {
    width: 50%;
    float: left;
    text-align: center;
}

video {
width: 600px;
}

label {
    color: white;
}

output {
    color: white;
}
.rightContainer {
 margin-top : 5%;
 text-align : center;
 float: right;
 width: 25%;
}

button {
    width : 75px;
    height: 50px;
}

h2 {
    color: white;
    font-family: 'Courier New', monospace;
}

canvas {
    margin-left: -25%
}

`;
let template = /*html*/`
<div class="main-container">
<div class="buttonContainer">
  <button id="play">PLAY</button>
  <br>
  <button id="pause">PAUSE</button>
  <br>
  <button id="avance10">+10s</button>
  <br>
  <button id="vitesse4" >Vitesse 4x</button>
  <br>
  <webaudio-knob id="volume" min=0 max=1 value=0.5 step="0.01" 
         tooltip="%s" diameter="75" src="./assets/aqua.png" sprites="100"></webaudio-knob>
   <br>
   <label>G</label><input type="range" min="-1" max="1" step="0.1" value="0" id="pannerSlider" /><label>D</label>
   </div>
<div class="videoContainer">
  <video id="player" crossorigin="anonymous">
  </video>

  <br>
     <canvas id="myCanvas" width=1200 height=100></canvas>
     <h2>WAVEFORM</h2>
</div>
<div class="rightContainer">
<h2>Egaliseur de fréquences</h2>
   <div class="controls">
    <label>60Hz</label>
    <input id="input60Hz" type="range" value="0" step="1" min="-30" max="30"/>
  <output id="gain0">0 dB</output>
  </div>
    <br>
  <div class="controls">
    <label>170Hz</label>
    <input id="input170Hz" type="range" value="0" step="1" min="-30" max="30"/>
<output id="gain1">0 dB</output>
  </div>
    <br>
  <div class="controls">
    <label>350Hz</label>
    <input id="input350Hz" type="range" value="0" step="1" min="-30" max="30"/>
<output id="gain2">0 dB</output>
  </div>
    <br>
  <div class="controls">
    <label>1000Hz</label>
    <input id="input1000Hz" type="range" value="0" step="1" min="-30" max="30"/>
<output id="gain3">0 dB</output>
  </div>
    <br>
  <div class="controls">
    <label>3500Hz</label>
    <input id="input3500Hz" type="range" value="0" step="1" min="-30" max="30"/>
<output id="gain4">0 dB</output>
  </div>
    <br>
  <div class="controls">
    <label>10000Hz</label>
    <input id="input10000Hz" type="range" value="0" step="1" min="-30" max="30"/>
<output id="gain5">0 dB</output>
  </div>
  
</div>
  </div>
   `;

class MyVideoPlayer extends HTMLElement {

    constructor() {
        super();

        console.log("BaseURL = " + getBaseURL());

        this.attachShadow({mode: "open"});
    }

    fixRelativeURLs() {
        // pour les knobs
        let knobs = this.shadowRoot.querySelectorAll('webaudio-knob, webaudio-switch, webaudio-slider');
        knobs.forEach((e) => {
            let path = e.getAttribute('src');
            e.src = getBaseURL() + '/' + path;
        });
    }

    connectedCallback() {

        // Appelée avant affichage du composant
        //this.shadowRoot.appendChild(template.content.cloneNode(true));
        this.shadowRoot.innerHTML = `<style>${style}</style>${template}`;

        this.fixRelativeURLs();

        this.player = this.shadowRoot.querySelector("#player");
        // récupération de l'attribut HTML
        this.player.src = this.getAttribute("src");

        // déclarer les écouteurs sur les boutons
        this.definitEcouteurs();

        this.player.onplay = (e) => {
            audioContext.resume();
        }

        this.pannerSlider = this.shadowRoot.querySelector('#pannerSlider');

        this.buildAudioGraphPanner();

        this.pannerSlider.oninput = (evt) => {
            this.pannerNode.pan.value = evt.target.value;
        };


        // Egaliseur

        this.filters = [];
        [60, 170, 350, 1000, 3500, 10000].forEach((freq, i) => {
            let eq = ctx.createBiquadFilter();
            eq.frequency.value = freq;
            eq.type = "peaking";
            eq.gain.value = 0;
            this.filters.push(eq);
        });

        this.sourceNode.connect(this.filters[0]);
        for (let i = 0; i < this.filters.length - 1; i++) {
            this.filters[i].connect(this.filters[i + 1]);
        }

        this.filters[this.filters.length - 1].connect(ctx.destination);

        //Visualiseur
        this.canvas = this.shadowRoot.querySelector("#myCanvas");
        this.canvasWidth = this.canvas.width;
        this.canvasHeight = this.canvas.height;
        this.canvasContext = this.canvas.getContext('2d');


        this.buildAudioGraphAnalyser();

        requestAnimationFrame(() => {
            this.visualize()
        });
    }

    definitEcouteurs() {
        console.log("ecouteurs définis")
        this.shadowRoot.querySelector("#play").onclick = () => {
            this.play();
        }
        this.shadowRoot.querySelector("#pause").onclick = () => {
            this.pause();
        }

        this.shadowRoot.querySelector("#avance10").onclick = () => {
            this.avance10();
        }

        this.shadowRoot.querySelector("#vitesse4").onclick = () => {
            this.vitesseX4();
        }

        this.shadowRoot.querySelector("#volume").oninput = (event) => {
            const vol = parseFloat(event.target.value);
            this.player.volume = vol;
        }

        this.shadowRoot.querySelector("#input60Hz").oninput = (event) => {
            this.changeGain(event.target.value, 0)
        }

        this.shadowRoot.querySelector("#input170Hz").oninput = (event) => {
            this.changeGain(event.target.value, 1)
        }

        this.shadowRoot.querySelector("#input350Hz").oninput = (event) => {
            this.changeGain(event.target.value, 2)
        }

        this.shadowRoot.querySelector("#input1000Hz").oninput = (event) => {
            this.changeGain(event.target.value, 3)
        }

        this.shadowRoot.querySelector("#input3500Hz").oninput = (event) => {
            this.changeGain(event.target.value, 4)
        }

        this.shadowRoot.querySelector("#input10000Hz").oninput = (event) => {
            this.changeGain(event.target.value, 5)
        }
    }

    // API de mon composant
    play() {
            this.player.play();
            this.shadowRoot.querySelector("#play").className = "playerClicked";
            this.shadowRoot.querySelector("#pause").className = "";
    }

    pause() {
            this.player.pause();
            this.shadowRoot.querySelector("#pause").className = "pauseClicked";
            this.shadowRoot.querySelector("#play").className = "";
    }

    avance10() {
        this.player.currentTime += 10;
    }

    vitesseX4() {
        if (this.player.playbackRate !== 4) {
            this.player.playbackRate = 4;
            this.shadowRoot.querySelector("#vitesse4").className = "vitesse4Clicked"
        } else {
            this.player.playbackRate = 1;
            this.shadowRoot.querySelector("#vitesse4").className = ""
        }
    }

    buildAudioGraphPanner() {
        this.sourceNode = audioContext.createMediaElementSource(this.player);
        this.pannerNode = audioContext.createStereoPanner();
        // connect nodes together
        this.sourceNode.connect(this.pannerNode);
        this.pannerNode.connect(audioContext.destination);
    }

    buildAudioGraphAnalyser() {
        this.analyser = ctx.createAnalyser()

        this.analyser.fftSize = 1024;
        this.bufferLength = this.analyser.frequencyBinCount;
        this.dataArray = new Uint8Array(this.bufferLength);

        this.filters[this.filters.length - 1].connect(this.analyser)

        this.analyser.connect(ctx.destination)
    }

    changeGain(sliderVal, nbFilter) {
        let value = parseFloat(sliderVal);
        this.filters[nbFilter].gain.value = value;

        // update output labels
        let output = this.shadowRoot.querySelector("#gain" + nbFilter);
        output.value = value + " dB";
    }

    visualize() {

        this.canvasContext.save();
        this.canvasContext.fillStyle = "rgba(0, 0, 0, 0.05)";
        this.canvasContext.fillRect (0, 0, this.canvasWidth, this.canvasHeight);

        this.analyser.getByteFrequencyData(this.dataArray);
        let nbFreq = this.dataArray.length;

        var SPACER_WIDTH = 5;
        var BAR_WIDTH = 2;
        var OFFSET = 100;
        var CUTOFF = 23;
        var HALF_HEIGHT = this.canvasHeight/2;
        var numBars = 1.7*Math.round(this.canvasWidth / SPACER_WIDTH);
        var magnitude;

        this.canvasContext.lineCap = 'round';

        for (var i = 0; i < numBars; ++i) {
            magnitude = 0.3*this.dataArray[Math.round((i * nbFreq) / numBars)];

            this.canvasContext.fillStyle = "hsl( " + Math.round((i*360)/numBars) + ", 100%, 50%)";
            this.canvasContext.fillRect(i * SPACER_WIDTH, HALF_HEIGHT, BAR_WIDTH, -magnitude);
            this.canvasContext.fillRect(i * SPACER_WIDTH, HALF_HEIGHT, BAR_WIDTH, magnitude);

        }

        // Draw animated white lines top
        this.canvasContext.strokeStyle = "white";
        this.canvasContext.beginPath();

        for (i = 0; i < numBars; ++i) {
            magnitude = 0.3*this.dataArray[Math.round((i * nbFreq) / numBars)];
            if(i > 0) {
                //console.log("line lineTo "  + i*SPACER_WIDTH + ", " + -magnitude);
                this.canvasContext.lineTo(i*SPACER_WIDTH, HALF_HEIGHT-magnitude);
            } else {
                //console.log("line moveto "  + i*SPACER_WIDTH + ", " + -magnitude);
                this.canvasContext.moveTo(i*SPACER_WIDTH, HALF_HEIGHT-magnitude);
            }
        }
        for (i = 0; i < numBars; ++i) {
            magnitude = 0.3*this.dataArray[Math.round((i * nbFreq) / numBars)];
            if(i > 0) {
                //console.log("line lineTo "  + i*SPACER_WIDTH + ", " + -magnitude);
                this.canvasContext.lineTo(i*SPACER_WIDTH, HALF_HEIGHT+magnitude);
            } else {
                //console.log("line moveto "  + i*SPACER_WIDTH + ", " + -magnitude);
                this.canvasContext.moveTo(i*SPACER_WIDTH, HALF_HEIGHT+magnitude);
            }
        }
        this.canvasContext.stroke();

        this.canvasContext.restore();

        requestAnimationFrame(() => {
            this.visualize()
        });
    }
}

customElements.define("my-player", MyVideoPlayer);
