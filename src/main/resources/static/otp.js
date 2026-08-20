let mobileNumber = "";

let countdown = 30;

let timerInterval;


/* OTP inputs */

const otpInputs = [
    document.getElementById("otp-0"),
    document.getElementById("otp-1"),
    document.getElementById("otp-2"),
    document.getElementById("otp-3"),
    document.getElementById("otp-4"),
    document.getElementById("otp-5")
];


/* Elements */

const sendButton =
    document.getElementById("sendButton");

const verifyButton =
    document.getElementById("verifyButton");

const resendButton =
    document.getElementById("resendButton");

const mobileInput =
    document.getElementById("mobileNumber");

const displayMobile =
    document.getElementById("displayMobile");

const errorMessage =
    document.getElementById("errorMessage");

const timer =
    document.getElementById("timer");


/* Send OTP */

sendButton.addEventListener(
    "click",
    sendOtp
);


async function sendOtp() {

    const mobile =
        mobileInput.value.trim();


    /* Validate mobile */

    if (!/^[6-9][0-9]{9}$/.test(mobile)) {

        showError(
            "Enter a valid 10-digit mobile number"
        );

        return;
    }


    mobileNumber = mobile;

    displayMobile.textContent =
        mobile;


    try {

        sendButton.disabled = true;

        sendButton.textContent =
            "Sending...";


        const response =
            await fetch(
                `/api/v1/otp/send?mobileNumber=${mobile}`,
                {
                    method: "POST"
                }
            );


        const data =
            await response.json();


        if (!response.ok) {

            showError(data.message);

            return;
        }


        clearError();

        alert(
            "OTP sent successfully. Check Spring Boot console."
        );


        startTimer();

        otpInputs[0].focus();


    } catch (error) {

        showError(
            "Unable to send OTP"
        );

    } finally {

        sendButton.disabled = false;

        sendButton.textContent =
            "Send OTP";
    }
}


/* Verify OTP */

verifyButton.addEventListener(
    "click",
    verifyOtp
);


async function verifyOtp() {

    const otp =
        otpInputs
            .map(input => input.value)
            .join("");


    if (otp.length !== 6) {

        showError(
            "Please enter the 6-digit OTP"
        );

        return;
    }


    try {

        verifyButton.disabled = true;

        verifyButton.textContent =
            "Verifying...";


        const response =
            await fetch(
                `/api/v1/otp/verify?mobileNumber=${mobileNumber}&otp=${otp}`,
                {
                    method: "POST"
                }
            );


        const data =
            await response.json();


        if (!response.ok) {

            showError(
                data.message
            );

            clearOtp();

            return;
        }


        clearError();


        alert(
            "Mobile number verified successfully!"
        );


        clearOtp();

        clearInterval(timerInterval);


    } catch (error) {

        showError(
            "Unable to verify OTP"
        );

    } finally {

        verifyButton.disabled = false;

        verifyButton.textContent =
            "Verify OTP";
    }
}


/* Resend */

resendButton.addEventListener(
    "click",
    sendOtp
);


/* OTP input handling */

otpInputs.forEach(
    (input, index) => {

        input.addEventListener(
            "input",
            () => {

                input.value =
                    input.value.replace(
                        /[^0-9]/g,
                        ""
                    );


                if (
                    input.value &&
                    index < otpInputs.length - 1
                ) {

                    otpInputs[index + 1].focus();

                }

            }
        );


        input.addEventListener(
            "keydown",
            (event) => {

                if (
                    event.key === "Backspace" &&
                    !input.value &&
                    index > 0
                ) {

                    otpInputs[index - 1].focus();

                }

            }
        );


        /* Paste OTP */

        input.addEventListener(
            "paste",
            (event) => {

                event.preventDefault();


                const pasted =
                    event.clipboardData
                        .getData("text")
                        .replace(
                            /[^0-9]/g,
                            ""
                        )
                        .substring(0, 6);


                pasted
                    .split("")
                    .forEach(
                        (digit, i) => {

                            if (otpInputs[i]) {

                                otpInputs[i].value =
                                    digit;

                            }

                        }
                    );


                if (pasted.length === 6) {

                    otpInputs[5].focus();

                }

            }
        );

    }
);


/* Timer */

function startTimer() {

    countdown = 30;

    resendButton.disabled = true;

    timer.textContent =
        `Resend available in ${countdown} seconds`;


    clearInterval(timerInterval);


    timerInterval =
        setInterval(
            () => {

                countdown--;


                if (countdown <= 0) {

                    clearInterval(
                        timerInterval
                    );

                    resendButton.disabled =
                        false;

                    timer.textContent =
                        "You can resend OTP";

                    return;
                }


                timer.textContent =
                    `Resend available in ${countdown} seconds`;

            },
            1000
        );
}


/* Clear OTP */

function clearOtp() {

    otpInputs.forEach(
        input => {
            input.value = "";
        }
    );

    otpInputs[0].focus();
}


/* Error */

function showError(message) {

    errorMessage.textContent =
        message;
}


/* Clear error */

function clearError() {

    errorMessage.textContent =
        "";
}