(function(b){var a=b.layout;if(!a.callbacks){a.callbacks={}}a.callbacks.resizePaneAccordions=function(c,e){var d=e.jquery?e:b(e.panel);d.find(".ui-accordion:visible").each(function(){var f=b(this);if(f.data("accordion")){f.accordion("resize")}})}})(jQuery);