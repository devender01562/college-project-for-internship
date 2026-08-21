// --- Security PINs Configuration ---
const ADMIN_LOGIN_PIN = "9999"; // Portal Login PIN
const ACTION_PIN = "66372";     // Edit aur Delete karne ke liye PIN

// --- Login Function ---
function checkLogin() {
    const pinInput = document.getElementById("adminPin").value;
    if (pinInput === ADMIN_LOGIN_PIN) {
        document.getElementById("login-container").style.display = "none";
        document.getElementById("dashboard-content").style.display = "block";
        loadData();
    } else {
        alert("❌ Galat Login PIN! Please try again.");
    }
}

function toggleDarkMode() {
    document.body.classList.toggle("dark-mode");
}

// --- Load Data ---
function loadData() {
    fetch('/api/users')
        .then(response => response.json())
        .then(data => {
            const table = document.getElementById('usersTable');
            table.innerHTML = '<tr><th>Emp ID</th><th>Name & Contact</th><th>Department</th><th>Status</th><th>Action</th></tr>';
            
            let total = data.length;
            let wfh = data.filter(d => d.work_status === 'WFH').length;
            let onLeave = data.filter(d => d.work_status === 'On Leave').length;
            
            document.getElementById('totalCount').innerText = total;
            document.getElementById('wfhCount').innerText = wfh;
            document.getElementById('leaveCount').innerText = onLeave;

            data.forEach(emp => {
                let statusIcon = emp.work_status === 'Office' ? '🏢' : (emp.work_status === 'WFH' ? '💻' : '🏖️');
                table.innerHTML += "<tr>" + 
                    "<td><strong>" + emp.emp_id + "</strong></td>" + 
                    "<td>" + emp.emp_name + "<br><small style='color:gray'>📞 " + emp.mobile + "</small></td>" + 
                    "<td>" + emp.department + "</td>" + 
                    "<td>" + statusIcon + " " + emp.work_status + "</td>" + 
                    "<td>" + 
                        "<button onclick=\"speakStatus('" + emp.emp_name + "', '" + emp.emp_id + "', '" + emp.department + "', '" + emp.work_status + "')\" class='btn-blue' style='background-color: #8e44ad;'>🔊 Audio</button>" +
                        "<button onclick=\"editUser(" + emp.id + ", '" + emp.emp_name + "', '" + emp.emp_id + "', '" + emp.department + "', '" + emp.work_status + "', '" + emp.mobile + "')\" class='btn-blue'>Edit</button>" + 
                        "<button onclick='deleteUser(" + emp.id + ")' class='btn-red'>X</button>" + 
                    "</td>" + 
                    "</tr>";
            });
        });
}

function speakStatus(name, id, dept, status) {
    window.speechSynthesis.cancel();
    let textToSpeak = "Employee " + name + ", ID " + id + ", is in the " + dept + " department. Current work status is " + status + ".";
    let speech = new SpeechSynthesisUtterance(textToSpeak);
    speech.rate = 0.9;
    window.speechSynthesis.speak(speech);
}

function searchTable() {
    let input = document.getElementById("searchInput").value.toLowerCase();
    let table = document.getElementById("usersTable");
    let tr = table.getElementsByTagName("tr");
    for (let i = 1; i < tr.length; i++) {
        let idCol = tr[i].getElementsByTagName("td")[0];
        let nameCol = tr[i].getElementsByTagName("td")[1];
        let deptCol = tr[i].getElementsByTagName("td")[2];
        if (idCol || nameCol || deptCol) {
            let textToSearch = (idCol.innerText + " " + nameCol.innerText + " " + deptCol.innerText).toLowerCase();
            tr[i].style.display = textToSearch.indexOf(input) > -1 ? "" : "none";
        }
    }
}

// --- Edit Record (Protected by ACTION_PIN) ---
function editUser(id, name, empId, dept, status, mobile) {
    let pin = prompt("🔐 Enter HR Admin PIN to Edit Record:");
    if (pin === null) return;
    if (pin !== ACTION_PIN) {
        alert("❌ Wrong PIN! Access Denied.");
        return;
    }

    document.getElementById("userId").value = id;
    document.getElementById("empNameInput").value = name;
    document.getElementById("empIdInput").value = empId;
    document.getElementById("departmentInput").value = dept;
    document.getElementById("workStatusInput").value = status;
    document.getElementById("mobileInput").value = mobile;
    const btn = document.getElementById("submitBtn");
    btn.innerText = "Update Record"; 
    btn.className = "btn-orange";
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// --- Delete Record (Protected by ACTION_PIN) ---
function deleteUser(dbId) {
    let pin = prompt("🔐 Enter HR Admin PIN to Delete Record:");
    if (pin === null) return;
    if (pin !== ACTION_PIN) {
        alert("❌ Wrong PIN! Access Denied.");
        return;
    }

    if(confirm("⚠️ Are you sure you want to remove this employee record?")) {
        fetch('/api/delete', { 
            method: 'POST', 
            headers: { 'Content-Type': 'application/json' }, 
            body: JSON.stringify({ id: dbId }) 
        })
        .then(response => response.json())
        .then(result => { 
            loadData(); 
        });
    }
}

// --- Save / Update Form Submit Event Listener ---
document.getElementById("addForm").addEventListener("submit", function(event) {
    event.preventDefault();
    const userId = document.getElementById("userId").value;
    let apiUrl = userId !== "" ? '/api/update' : '/api/register';
    
    fetch(apiUrl, {
        method: 'POST', 
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
            id: String(userId), 
            emp_name: document.getElementById("empNameInput").value, 
            emp_id: document.getElementById("empIdInput").value, 
            department: document.getElementById("departmentInput").value,
            work_status: document.getElementById("workStatusInput").value, 
            mobile: document.getElementById("mobileInput").value
        })
    }).then(response => response.json()).then(result => {
        if(result.status === "success") {
            document.getElementById("addForm").reset(); 
            document.getElementById("userId").value = "";
            const btn = document.getElementById("submitBtn"); 
            btn.innerText = "💾 Save Record"; 
            btn.className = "btn-green";
            loadData();
        } else {
            alert(result.message);
        }
    }).catch(err => {
        console.error("Save error:", err);
    });
});

function downloadExcel() { 
    window.location.href = '/api/export'; 
}