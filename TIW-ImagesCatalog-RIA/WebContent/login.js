/** 
 * Login management
 */

 (function(){

    document.getElementById("loginButton").addEventListener('click', (e)=>{

        var form = e.target.closest("form");

        if (form.checkValidity()){

            makeCall("POST", 'CheckLogin', e.target.closest("form"),
            function(req){
                
                if (req.readyState == XMLHttpRequest.DONE){

                    var message = req.responseText;

                    switch (req.status){

                        case 200: //OK
                            var user = JSON.parse(message);
                            sessionStorage.setItem("username", user.username);
                            sessionStorage.setItem("name", user.name);
                            sessionStorage.setItem("surname", user.surname);
                            window.location.href = "home.html";
                            break;

                        case 400: //Bad request

                            document.getElementById("errorMsg").textContent = message;
                            break;

                        case 401: //Unauthorized
                            document.getElementById("errorMsg").textContent = message;
                            break;
                        
                        case 500: //Internal server error

                        document.getElementById("errorMsg").textContent = message;
                        break;

                    }

                }
            }
            );
        } else {

            form.reportValidity();
        
        }
    });

 })();