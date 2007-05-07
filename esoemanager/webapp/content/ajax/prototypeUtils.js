/*
prototypeUtils.js from http://jehiah.com/
Licensed under Creative Commons.
version 1.0 December 20 2005

Contains:
+ Form.Element.setValue()
+ unpackToForm()

*/

/* Form.Element.setValue("fieldname/id","valueToSet") */
Form.Element.setValue = function(element,newValue) {
    element_id = element;
    element = $(element);
    if (!element){element = document.getElementsByName(element_id)[0];}
    if (!element){return false;}
    var method = element.tagName.toLowerCase();
    var parameter = Form.Element.SetSerializers[method](element,newValue);
}

Form.Element.SetSerializers = {
  input: function(element,newValue) {
    switch (element.type.toLowerCase()) {
      case 'submit':
      case 'hidden':
      case 'password':
      case 'text':
        return Form.Element.SetSerializers.textarea(element,newValue);
      case 'checkbox':
      case 'radio':
        return Form.Element.SetSerializers.inputSelector(element,newValue);
    }
    return false;
  },

  inputSelector: function(element,newValue) {
    fields = document.getElementsByName(element.name);
    for (var i=0;i<fields.length;i++){
      if (fields[i].value == newValue){
        fields[i].checked = true;
      }
    }
  },

  textarea: function(element,newValue) {
    element.value = newValue;
  },

  select: function(element,newValue) {
    var value = '', opt, index = element.selectedIndex;
    for (var i=0;i< element.options.length;i++){
      if (element.options[i].value == newValue){
        element.selectedIndex = i;
        return true;
      }        
    }
  }
}

function unpackToForm(data){
   for (i in data){
     Form.Element.setValue(i,data[i].toString());
   }
}
