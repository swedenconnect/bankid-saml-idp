$(document).ready(

    function() {
      
      // $('.drop-down-container').show();     
      $('#helpsection').show(); 

      var selectedUser = $('#selectSimulatedUser').val();
      if (selectedUser == 'NONE') {        
        $('#authenticateButton').attr('disabled', 'disabled');
        if (isAdvancedValid()) {
          $('#advancedSettings').show();
          $('#selectSimulatedUser').attr('disabled', 'disabled');
          $('#advancedButton').hide();
        }
      }
      else {
        $('#personalIdNumber').val('');
        $('#givenName').val('');
        $('#surname').val('');
      }

      // We only support "Advanced" if the agent support JS
      $('#advancedButton').parent().show();

      $('#advancedButton').click(function() {
        if ($('#advancedSettings').is(':hidden')) {
          $('#advancedSettings').show();
          $('#selectSimulatedUser').val("NONE");
          $('#selectSimulatedUser').attr('disabled', 'disabled');
          $('#advancedButton').hide();
          if (!isAdvancedValid()) {
            $('#submitButton').attr('disabled', 'disabled');
          }
          else {
            $('#submitButton').removeAttr('disabled');
          }
        }
      });

      $('#cancelAdvancedButton').click(function() {
        $('#advancedSettings').hide();
        $('#selectSimulatedUser').removeAttr('disabled');
        $('#advancedButton').show();
        $('#selectSimulatedUser').val(selectedUser);
        if (selectedUser != 'NONE') {
          $('#submitButton').removeAttr('disabled');
        }
        else {
          $('#submitButton').attr('disabled', 'disabled');
        }
      });

      $('#selectSimulatedUser').change(function() {
        selectedUser = $(this).val();

        if (selectedUser != 'NONE') {
          $('#submitButton').removeAttr('disabled');
        }
        else {
          $('#submitButton').attr('disabled', 'disabled');
        }
      });

      $('.drop-down > p').click(function() {
        $(this).parent('.drop-down').toggleClass('open');
      });

      $('#personalIdNumber').on("change paste keyup", function() {
        if (isAdvancedValid()) {
          $('#submitButton').removeAttr('disabled');
        }
        else {
          $('#submitButton').attr('disabled', 'disabled');
        }
      });

      $('#givenName').on("change paste keyup", function() {
        if (isAdvancedValid()) {
          $('#submitButton').removeAttr('disabled');
        }
        else {
          $('#submitButton').attr('disabled', 'disabled');
        }
      });

      $('#surname').on("change paste keyup", function() {
        if (isAdvancedValid()) {
          $('#submitButton').removeAttr('disabled');
        }
        else {
          $('#submitButton').attr('disabled', 'disabled');
        }
      });

      function isAdvancedValid() {
        var pnr = personalIdNumber();
        if (!pnr) {
          return false;
        }
        else {
          $('#personalIdNumber').val(pnr);
        }

        if ($('#givenName').val().trim() == ''
            && $('#surname').val().trim() == '') {
          for (var i = 0; i < users.length; i++) {
            if (users[i].pnr == pnr) {
              $('#givenName').val(users[i].givenName);
              $('#surname').val(users[i].surname);
              break;
            }
          }
        }
        if ($('#givenName').val().trim().length > 0
            && $('#surname').val().trim().length > 0) {
          return true;
        }
        return false;
      }

      function personalIdNumber() {
        var pnr = $('#personalIdNumber').val();
        $('#personalIdNumber').removeClass('is-invalid');

        if (pnr.length == 0) {
          return false;
        }
        else if (pnr.length < 12
            || (pnr.length == 12 && pnr.indexOf('-') != -1)) {
          return false;
        }
        else {
          var res = valfor.personalidnum(pnr, valfor.NBR_DIGITS_12);
          if (res == false) {
            $('#personalIdNumber').addClass('is-invalid');
            $('#badPersonalIdNumber').show();
            return false;
          }
          else {
            $('#personalIdNumber').removeClass('is-invalid');
            $('#badPersonalIdNumber').hide();
            return res;
          }
        }
      }

    });
