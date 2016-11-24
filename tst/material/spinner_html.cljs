(ns material.spinner-html)

(def mr-rocks
  "Material Design Spinner from https://codepen.io/mrrocks/pen/EiplA"

  "<style type=\"text/css\">
        .spinner {
            animation: rotator 1.4s linear infinite;
        }

        @keyframes rotator {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(270deg); }
        }

        .path {
            stroke-dasharray: 187;
            stroke-dashoffset: 0;
            transform-origin: center;
            animation:
            dash 1.4s ease-in-out infinite,
            colors 5.6s ease-in-out infinite;
        }

        @keyframes colors {
            0% { stroke: #4285F4; }
            25% { stroke: #DE3E35; }
            50% { stroke: #F7C223; }
            75% { stroke: #1B9A59; }
            100% { stroke: #4285F4; }
        }

        @keyframes dash {
            0% { stroke-dashoffset: 187; }
            50% {
                stroke-dashoffset: 47;
                transform:rotate(135deg);
            }
            100% {
                stroke-dashoffset: 187;
                transform:rotate(450deg);
            }
        }
      </style>

      <svg class=\"spinner\" width=\"65\" height=\"65\"
       viewBox=\"0 0 66 66\" xmlns=\"http://www.w3.org/2000/svg\">
      <circle class=\"path\" fill=\"none\" stroke-width=\"6\" stroke-linecap=\"round\"
       cx=\"33\" cy=\"33\" r=\"30\"></circle></svg>")

(def loading-io
  "From http://loading.io/"

  "<svg width='120px' height='120px' xmlns=\"http://www.w3.org/2000/svg\"
     viewBox=\"0 0 100 100\" preserveAspectRatio=\"xMidYMid\" class=\"uil-default\">

     <rect x=\"0\" y=\"0\" width=\"100\" height=\"100\" fill=\"none\" class=\"bk\">
     </rect>

     <rect  x='46.5' y='40' width='7' height='20' rx='5' ry='5' fill='#00b2ff'
       transform='rotate(0 50 50) translate(0 -30)'>

       <animate attributeName='opacity' from='1' to='0' dur='1s'
        begin='0s' repeatCount='indefinite'/>

     </rect>

     <rect  x='46.5' y='40' width='7' height='20' rx='5' ry='5' fill='#00b2ff'
       transform='rotate(30 50 50) translate(0 -30)'>

       <animate attributeName='opacity' from='1' to='0' dur='1s'
        begin='0.08333333333333333s' repeatCount='indefinite'/>

     </rect>

     <rect  x='46.5' y='40' width='7' height='20' rx='5' ry='5' fill='#00b2ff'
       transform='rotate(60 50 50) translate(0 -30)'>

       <animate attributeName='opacity' from='1' to='0' dur='1s'
        begin='0.16666666666666666s' repeatCount='indefinite'/>

     </rect>

     <rect  x='46.5' y='40' width='7' height='20' rx='5' ry='5' fill='#00b2ff'
       transform='rotate(90 50 50) translate(0 -30)'>

       <animate attributeName='opacity' from='1' to='0' dur='1s'
        begin='0.25s' repeatCount='indefinite'/>

     </rect>

     <rect  x='46.5' y='40' width='7' height='20' rx='5' ry='5' fill='#00b2ff'
       transform='rotate(120 50 50) translate(0 -30)'>

       <animate attributeName='opacity' from='1' to='0' dur='1s'
        begin='0.3333333333333333s' repeatCount='indefinite'/>

     </rect>

     <rect  x='46.5' y='40' width='7' height='20' rx='5' ry='5' fill='#00b2ff'
       transform='rotate(150 50 50) translate(0 -30)'>

       <animate attributeName='opacity' from='1' to='0' dur='1s'
        begin='0.4166666666666667s' repeatCount='indefinite'/>

     </rect>

     <rect  x='46.5' y='40' width='7' height='20' rx='5' ry='5' fill='#00b2ff'
       transform='rotate(180 50 50) translate(0 -30)'>

       <animate attributeName='opacity' from='1' to='0' dur='1s'
        begin='0.5s' repeatCount='indefinite'/>

     </rect>

     <rect  x='46.5' y='40' width='7' height='20' rx='5' ry='5' fill='#00b2ff'
       transform='rotate(210 50 50) translate(0 -30)'>

       <animate attributeName='opacity' from='1' to='0' dur='1s'
        begin='0.5833333333333334s' repeatCount='indefinite'/>

     </rect>

     <rect  x='46.5' y='40' width='7' height='20' rx='5' ry='5' fill='#00b2ff'
       transform='rotate(240 50 50) translate(0 -30)'>

       <animate attributeName='opacity' from='1' to='0' dur='1s'
        begin='0.6666666666666666s' repeatCount='indefinite'/>

     </rect>

     <rect  x='46.5' y='40' width='7' height='20' rx='5' ry='5' fill='#00b2ff'
       transform='rotate(270 50 50) translate(0 -30)'>

       <animate attributeName='opacity' from='1' to='0' dur='1s'
        begin='0.75s' repeatCount='indefinite'/>

     </rect>

     <rect  x='46.5' y='40' width='7' height='20' rx='5' ry='5' fill='#00b2ff'
       transform='rotate(300 50 50) translate(0 -30)'>

       <animate attributeName='opacity' from='1' to='0' dur='1s'
        begin='0.8333333333333334s' repeatCount='indefinite'/>

     </rect>

     <rect  x='46.5' y='40' width='7' height='20' rx='5' ry='5' fill='#00b2ff'
       transform='rotate(330 50 50) translate(0 -30)'>

       <animate attributeName='opacity' from='1' to='0' dur='1s'
        begin='0.9166666666666666s' repeatCount='indefinite'/>

     </rect>
   </svg>")
