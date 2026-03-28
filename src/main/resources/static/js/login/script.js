let emailElement = document.getElementById("email");
let passwordElement = document.getElementById("password");
let errorElement = document.getElementById("error");

checkCookies();

function switchToReg() {
    window.location.href = window.location.origin + '/reg';
}

function sendToMessenger() {
    window.location.href = window.location.origin;
}

function sendData() {
    if (emailElement.value.length >= 14
        && /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/.test(emailElement.value)
        && passwordElement.value.length >= 8
        && /^[a-zA-Z0-9-_]+$/.test(passwordElement.value)) {

        let data = {
            mail: emailElement.value,
            password: passwordElement.value
        }

        fetch('/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        })
        .then(response => {
            return response.json().then(data => ({
                ok: response.ok,
                data: data
            }));
        })
        .then(({ ok, data }) => {
            if (!ok) {
                errorElement.textContent = data.message;
            } else {
                sendToMessenger();
            }
        })
        .catch(error => {
            console.error('Fetch error:', error);
        });

    } else {
        errorElement.textContent = "Некорректный ввод";
    }
}

function checkCookies() {
    let cookie = getCookie("session");

    if (cookie !== undefined) {
        fetch('/login-cookies')
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                if (data.validate) {
                    sendToMessenger()
                }
            }).catch(error => {
                console.error('Fetch error:', error);
            }
        );
    }
}

emailElement.addEventListener("input", function(e) {
    let value = emailElement.value;

    if (value.length >= 14 && /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/.test(value)) {
        emailElement.classList.remove("not-avalable");
    } else {
        emailElement.classList.add("not-avalable");
    }
});

passwordElement.addEventListener("input", function(e) {
    let value = passwordElement.value;

    if (value.length >= 8 && /^[a-zA-Z0-9-_]+$/.test(value)) {
        passwordElement.classList.remove("not-avalable");
    } else {
        passwordElement.classList.add("not-avalable");
    }
});

