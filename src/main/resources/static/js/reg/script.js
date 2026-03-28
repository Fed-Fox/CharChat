let emailElement = document.getElementById("email");
let passwordElement = document.getElementById("password");
let tagElement = document.getElementById("tag");
let nameElement = document.getElementById("name");
let errorElement = document.getElementById("error");

checkCookies();

function switchToLog() {
    window.location.href = window.location.origin + '/login';
}

function sendToMessenger() {
    window.location.href = window.location.origin;
}

function validate() {
    return emailElement.value.length >= 14
        && /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/.test(emailElement.value)
        && passwordElement.value.length >= 8
        && /^[a-zA-Z0-9-_]+$/.test(passwordElement.value)
        && tagElement.value.length >= 5
        && /[a-zA-Z0-9_-]+/.test(tagElement.value)
        && nameElement.value.length >= 6
        && /[a-z0-9_-]+/.test(nameElement.value);
}

function sendData() {
    if (validate()) {
        let data = {
            mail: emailElement.value,
            password: passwordElement.value,
            tag: tagElement.value,
            displayName: nameElement.value
        }

        console.info(data)

        fetch('/reg', {
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

tagElement.addEventListener("input", function(e) {
    let value = tagElement.value;

    if (value.length >= 5 && /[a-z0-9_-]+/.test(value)) {
        tagElement.classList.remove("not-avalable");
    } else {
        tagElement.classList.add("not-avalable");
    }
});

nameElement.addEventListener("input", function(e) {
    let value = nameElement.value;

    if (value.length >= 6 && /[a-zA-Z0-9_-]+/.test(value)) {
        nameElement.classList.remove("not-avalable");
    } else {
        nameElement.classList.add("not-avalable");
    }
});

