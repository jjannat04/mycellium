async function signup() {

    const user = {
        name: document.getElementById("name").value,
        email: document.getElementById("email").value,
        password: document.getElementById("password").value
    };

    const response = await fetch("http://localhost:8080/auth/signup", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(user)
    });

    const result = await response.text();

    alert(result);

    if(result === "User registered successfully"){
        window.location.href = "login.html";
    }
}

async function login() {

    const user = {
        email: document.getElementById("email").value,
        password: document.getElementById("password").value
    };

    const response = await fetch("http://localhost:8080/auth/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(user)
    });

    const result = await response.text();

    if(result === "Login successful"){

        localStorage.setItem("loggedInUser", user.email);

        window.location.href = "index.html";

    } else {

        alert(result);
    }
}