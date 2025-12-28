SVGInject.setOptions({
  afterInject: function(img, svg) {
    if (!svg.classList.contains('uml-class')) {
      /*
        Whether touch (rather than mouse pointer) input is available.

        NOTE: In this configuration, only mouse pointer input supports zoom and pan, whilst touch
        input simply leverages existing gestures to resize and move the whole page.
       */
      var SUPPORT_TOUCH = ('ontouchstart' in window);

      var panZoom = svgPanZoom(svg, {
        zoomEnabled: false,
        zoomScaleSensitivity: .4,
        dblClickZoomEnabled: false,
        controlIconsEnabled: !SUPPORT_TOUCH,
        fit: 1,
        center: 1,
        customEventsHandler: {
          haltEventListeners: ['touchstart', 'touchend', 'touchmove', 'touchleave', 'touchcancel'],
          init: function(options) {
            // Mouse pointer input?
            if (!SUPPORT_TOUCH) {
              var svgHovering = false
              var svgZooming = false
              var svgLastGrabTime;

              $(svg).find("a").click(function() {
                /*
                  Prevent parasitic interaction with SVG links on grab ending!

                  NOTE: Grabbing the SVG while the pointer is above an inner linked object triggers
                  the navigation to its target as soon as the SVG is released; to avoid such
                  parasitic interaction, in order to navigate there must be a significant delay
                  (500+ ms, whilst grab-related event is typically 10- ms) between the end of the
                  last grab and the click on the link.
                */
                return Date.now() - svgLastGrabTime > 500;
              });

              this.listeners = {
                mousedown: function(){
                  if(document.body.style.cursor === 'grab'){
                    // Save grab starting time for duration check on ending!
                    svgLastGrabTime = Date.now()

                    document.body.style.cursor = 'grabbing'
                  }
                },
                mouseenter: function() {
                  document.body.style.cursor = 'grab'
                  svgHovering = true
                },
                mouseleave: function() {
                  svgHovering = false
                  document.body.style.cursor = 'default'

                  svgZooming = false
                  options.instance.disableZoom()
                },
                mouseup: function() {
                  if(document.body.style.cursor === 'grabbing'){
                    /*
                      Save grab ending time for delay check on SVG link click!

                      NOTE: Only a significantly long (200+ ms) grab is considered as such;
                      otherwise, it degrades to simple click applicable to SVG link navigation.
                      Consequently, the former (actual grab) is applied a zero delay, whilst the
                      latter (simple click) 1,000 ms (for further information, see click event for
                      SVG anchors here above).
                    */
                    svgLastGrabTime = Date.now() - (Date.now() - svgLastGrabTime > 200 ? 0 : 1000)

                    document.body.style.cursor = 'grab'
                  }
                }
              }
              for (var eventName in this.listeners) {
                options.svgElement.addEventListener(eventName, this.listeners[eventName])
              }

              window.addEventListener("keydown", function(e) {
                if (svgHovering && e.key === 'Shift') {
                  /*
                    Prevent parasitic interaction with the search bar while zooming!

                    NOTE: Pressing the shift key, the search results' dropdown list pops up whenever
                    the search field is non-empty; to avoid such parasitic interaction, the event
                    propagation is stopped.
                  */
                  e.stopPropagation()

                  options.instance.enableZoom()
                  svgZooming = true

                  document.body.style.cursor = 'zoom-in'
                }
              }, true /* capture */)
              window.addEventListener("keyup", function(e) {
                if (svgZooming && e.key === 'Shift') {
                  options.instance.disableZoom()
                  svgZooming = false

                  document.body.style.cursor = 'grab'
                }
              }, true /* capture */)
            }
          },
          destroy: function(options) {
            // Mouse pointer input?
            if (!SUPPORT_TOUCH) {
              for (var eventName in this.listeners) {
                options.svgElement.removeEventListener(eventName, this.listeners[eventName])
              }
            }
          }
        }
      });

      $(window).resize(function() {
        panZoom.resize()
        panZoom.fit()
        panZoom.center()
      })
    }
  }
});