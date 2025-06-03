i=1
while mvn test; do
  echo "✅ Run #$i passed"
  ((i++))
done

echo "❌ Failed on run #$i"