/**
 * Created by Dennis on 28-1-2017.
 */

var yearLow = 2011;
var yearHigh = 2011;

function updateSlider(){
    d3.select('#slider').selectAll('li').each(function (d, i) {
        var el = d3.select(this);
        if(i + 2011 >= yearLow && i + 2011 <= yearHigh){
            el.attr('class', 'page-item active');
        } else {
            el.attr('class', 'page-item');
        }
    });
}
updateSlider();

function changeSlider(data,index){
    var year = index + 2011;
    yearLow = year;
    yearHigh = year;
    updateSlider();
}

d3.select('#slider').selectAll('li').on('click', changeSlider);

